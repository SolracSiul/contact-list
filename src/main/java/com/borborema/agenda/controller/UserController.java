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
        userService.createUser(userDto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("login")
    public ResponseEntity login(@RequestBody UserAuthenticationDTO authenticationDTO){
       String token =  userService.authenticate(authenticationDTO);
       return ResponseEntity.ok(token);
    }

    @GetMapping("list")
    public ResponseEntity<List<UserDTO>> getUsers (){
       List<User> users = userService.listUsers();

       List<UserDTO> usersDTO = User.entityListToDto(users);

       return ResponseEntity.ok(usersDTO);
    }

}
