package com.devArenaBackend.Controller;

import com.devArenaBackend.DTO.RequestDto;
import com.devArenaBackend.Service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins="http://localhost:5173")
public class AuthController {
    private final AuthService authService;
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RequestDto user) {
        return new ResponseEntity<>(authService.register(user), HttpStatus.OK);
    }
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody RequestDto user) {
        return new ResponseEntity<>(authService.login(user), HttpStatus.OK);
    }


}
