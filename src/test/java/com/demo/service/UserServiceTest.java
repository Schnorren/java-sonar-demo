package com.demo.service;

import com.demo.exception.DuplicateResourceException;
import com.demo.exception.ResourceNotFoundException;
import com.demo.model.User;
import com.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("john");
        testUser.setEmail("john@example.com");
        testUser.setPassword("hashedPassword");
        testUser.setRole(User.Role.USER);
        testUser.setActive(true);
    }

    @Test
    @DisplayName("Should find user by ID successfully")
    void findById_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        User result = userService.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("john");
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void findById_notFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("Should create user with encoded password")
    void create_success() {
        User newUser = new User();
        newUser.setUsername("jane");
        newUser.setEmail("jane@example.com");
        newUser.setPassword("plainPassword");

        when(userRepository.existsByUsername("jane")).thenReturn(false);
        when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(passwordEncoder.encode("plainPassword")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(2L);
            return u;
        });

        User created = userService.create(newUser);

        assertThat(created.getPassword()).isEqualTo("encodedPassword");
        assertThat(created.getRole()).isEqualTo(User.Role.USER);
        verify(passwordEncoder).encode("plainPassword");
    }

    @Test
    @DisplayName("Should throw exception when username already exists")
    void create_duplicateUsername() {
        User newUser = new User();
        newUser.setUsername("john");
        newUser.setEmail("other@example.com");
        newUser.setPassword("pass");

        when(userRepository.existsByUsername("john")).thenReturn(true);

        assertThatThrownBy(() -> userService.create(newUser))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("john");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void create_duplicateEmail() {
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setEmail("john@example.com");
        newUser.setPassword("pass");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.create(newUser))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("john@example.com");
    }

    @Test
    @DisplayName("Should soft-delete user")
    void delete_setsInactive() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.delete(1L);

        assertThat(testUser.isActive()).isFalse();
        verify(userRepository).save(testUser);
    }
}
