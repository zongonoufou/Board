package com.example.Board.repository;

import com.example.Board.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * @Query explicite : Task expose un getter JSON {@code getBoardId()}
     * qui empecherait Spring Data de deriver correctement "findByBoardId".
     */
    @Query("select t from Task t where t.board.id = :boardId")
    List<Task> findByBoardId(@Param("boardId") Long boardId);

    /**
     * Verifie a la fois l'existence de la tache ET son appartenance au
     * tableau : evite d'acceder a une tache via l'URL d'un autre tableau.
     */
    @Query("select t from Task t where t.id = :id and t.board.id = :boardId")
    Optional<Task> findByIdAndBoardId(@Param("id") Long id,
                                      @Param("boardId") Long boardId);
}
