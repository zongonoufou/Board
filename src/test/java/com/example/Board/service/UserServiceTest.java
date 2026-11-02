package com.example.Board.service;

import com.example.Board.exception.DuplicateResourceException;
import com.example.Board.exception.ResourceNotFoundException;
import com.example.Board.model.User;
import com.example.Board.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private static User user(String email) {
        User user = new User();
        user.setFirstName("Ada");
        user.setLastName("Lovelace");
        user.setEmail(email);
        return user;
    }

    @Test
    void createUserNormaliseEmailEtIgnoreLIdFourni() {
        User user = user("  ADA@Example.COM ");
        user.setId(99L);
        when(userRepository.existsByEmail("ada@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User created = userService.createUser(user);

        assertThat(created.getEmail()).isEqualTo("ada@example.com");
        assertThat(created.getId()).isNull();
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUserRefuseUnEmailDejaUtilise() {
        when(userRepository.existsByEmail("ada@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(user("ada@example.com")))
                .isInstanceOf(DuplicateResourceException.class);

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUserRefuseUnPrenomVide() {
        User user = user("ada@example.com");
        user.setFirstName("   ");

        assertThatThrownBy(() -> userService.createUser(user))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getUserByIdLeveUneExceptionMetierQuandAbsent() {
        when(userRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(42L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateUserEstPartielEtGardeSonPropreEmail() {
        User existing = user("ada@example.com");
        existing.setId(1L);
        User patch = new User();
        patch.setLastName("Byron");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updated = userService.updateUser(1L, patch);

        assertThat(updated.getLastName()).isEqualTo("Byron");
        assertThat(updated.getFirstName()).isEqualTo("Ada");
        assertThat(updated.getEmail()).isEqualTo("ada@example.com");
    }
}
