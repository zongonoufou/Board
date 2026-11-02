package com.example.Board.repository;

import com.example.Board.model.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BoardRepository extends JpaRepository<Board, Long> {

    /**
     * Requete explicite plutot qu'une query deriving "findByOwnerId" :
     * l'entite expose un getter de lecture seule {@code getOwnerId()} pour
     * l'API JSON. Spring Data le voit alors comme une propriete JavaBeans
     * et cherche un ATTRIBUT JPA "ownerId" qui n'existe pas -> erreur au
     * demarrage. La JPQL leve toute ambiguite en naviguant owner.id.
     */
    @Query("select b from Board b where b.owner.id = :ownerId")
    List<Board> findByOwnerId(@Param("ownerId") Long ownerId);
}
