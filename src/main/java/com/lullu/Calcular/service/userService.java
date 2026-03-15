package com.lullu.Calcular.service;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.lullu.Calcular.model.userModel;
import com.lullu.Calcular.repository.userRepository;

@Service
public class userService {

@Autowired
private final userRepository userrepository;

    public userService (userRepository userrepository) {
        this.userrepository = userrepository;
    }

    //Listar
    public List<userModel> getAll() {
        return userrepository.findAll();
    }
    //Criar
    public userModel save(userModel user) {         
        return userrepository.save(user);
    }

    //Deletar
    public void delete(Long id) {
        userrepository.deleteById(id);
    }

    // Buscar por email
    public Optional<userModel> findByEmail(String email) {
        return userrepository.findByEmail(email);
    }



}
