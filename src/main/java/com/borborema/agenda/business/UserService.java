package com.borborema.agenda.business;

import com.borborema.agenda.configuration.security.TokenService;
import com.borborema.agenda.infrastructure.entitys.user.User;
import com.borborema.agenda.infrastructure.models.UserAuthenticationDTO;
import com.borborema.agenda.infrastructure.models.UserRegisterDTO;
import com.borborema.agenda.infrastructure.repository.UserRepository;
import com.borborema.agenda.infrastructure.util.CriptoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Base64;
import java.util.List;

@Service
public class UserService {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TokenService tokenService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    CriptoService criptoService;

    public String createUser (UserRegisterDTO userDto)  {

        String encryptedPassword = passwordEncoder.encode(userDto.password());
        User newUser = new User(userDto);
        newUser.setPassword(encryptedPassword);

        try {
           KeyPair keyPair = criptoService.generateRSAKeyPar();
           newUser.setPrivateKey(Base64.getEncoder().encodeToString(
                   keyPair.getPrivate().getEncoded()
           ));
           this.userRepository.save(newUser);
           return  Base64.getEncoder().encodeToString(
                   keyPair.getPublic().getEncoded()
           );

        } catch (Exception e ){
            return "Não foi possivel gerar par de chaves";
        }

    }

    public String authenticate (UserAuthenticationDTO authenticationDTO) {
        UsernamePasswordAuthenticationToken usernamePassword = new UsernamePasswordAuthenticationToken(authenticationDTO.email(), authenticationDTO.password());
        Authentication authenticate = this.authenticationManager.authenticate(usernamePassword);

        String token = tokenService.generateToken((User) authenticate.getPrincipal());

        return token;
    }

    public List<User> listUsers() {
        List<User> users = this.userRepository.findAll();
        return users;
    }
}
