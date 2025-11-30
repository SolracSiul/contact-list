package com.borborema.agenda.controller;


import com.borborema.agenda.business.UserService;

import com.borborema.agenda.infrastructure.entitys.user.User;
import com.borborema.agenda.infrastructure.models.UserAuthenticationDTO;
import com.borborema.agenda.infrastructure.models.UserDTO;
import com.borborema.agenda.infrastructure.models.UserRegisterDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    @Autowired
    UserService userService;

    @PostMapping("create")
    public ResponseEntity createUser(@RequestBody UserRegisterDTO userDto){
        try {
            String publicKey =  userService.createUser(userDto);
            return ResponseEntity.ok(publicKey);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body("Não foi possível cadastrar o usuario");
        }
    }

    @PostMapping("login")
    public ResponseEntity<String> login(@RequestBody UserAuthenticationDTO authenticationDTO){

        try{
            String token =  userService.authenticate(authenticationDTO);
            return ResponseEntity.ok(token);
        } catch (Exception ex){
            return ResponseEntity.badRequest().body("Não foi possível realizar o login");
        }
    }

    @GetMapping("list")
    public ResponseEntity<List<UserDTO>> getUsers (){
       List<User> users = userService.listUsers();

       List<UserDTO> usersDTO = User.entityListToDto(users);

       return ResponseEntity.ok(usersDTO);
    }

}
