package com.devArenaBackend.Service;

import com.devArenaBackend.DTO.RequestDto;
import com.devArenaBackend.DTO.ResponseDto;
import com.devArenaBackend.Repository.UserRepository;
import com.devArenaBackend.config.JwtConfig;
import com.devArenaBackend.entity.Role;
import com.devArenaBackend.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final JwtConfig jwtConfig;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    public AuthService(UserRepository userRepository, JwtConfig jwtConfig, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.jwtConfig = jwtConfig;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }
    @Value("${admin.secret-key}")
    private String adminSecretKey;


    public ResponseDto register(RequestDto user) {
        if(userRepository.existsByEmail(user.getEmail()))  {
            throw new RuntimeException("Email already exists");
        }
        User userEntity = new User();
        userEntity.setEmail(user.getEmail());
        userEntity.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        userEntity.setName(user.getName());
        if(user.getAdminSecretKey()!=null && !user.getAdminSecretKey().isEmpty() && user.getAdminSecretKey().equals(adminSecretKey))  {
         userEntity.setRole(Role.ADMIN);
        }else{
            userEntity.setRole(Role.USER);
        }
        userRepository.save(userEntity);
        String token=jwtConfig.generateToken(user.getEmail(),userEntity.getRole().name());
        return new ResponseDto(user.getName(),user.getEmail(),token);


    }
    public ResponseDto login(RequestDto requestDto) {
        User user=userRepository.findByEmail(requestDto.getEmail()).orElseThrow(()->new RuntimeException("User Not Found"));
        if(!bCryptPasswordEncoder.matches(requestDto.getPassword(),user.getPassword())) {
            throw new RuntimeException("Wrong Password");
        }
        String token=jwtConfig.generateToken(user.getEmail(),user.getRole().name());
        return new ResponseDto(user.getName(),user.getEmail(),token);

    }
}
