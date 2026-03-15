package com.lullu.Calcular.repository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.lullu.Calcular.model.userModel;

@Repository
public interface userRepository extends JpaRepository<userModel, Long> {

    //Método para buscar usuário por email
    Optional<userModel> findByEmail(String email);

}
