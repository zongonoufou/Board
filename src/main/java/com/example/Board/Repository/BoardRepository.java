package com.example.Board.repository;
import com.example.Board.model.Board;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, Long> {

    /**
     * Query method derivee : Spring Data JPA traduit le nom de la methode
     * en : SELECT * FROM boards WHERE owner_id = ?
     *
     * "findByOwnerId" navigue directement dans la relation owner (User)
     * jusqu'a son champ id, sans avoir besoin d'ecrire de JPQL.
     */
    List<Board> findByOwnerId(Long ownerId);    
}
