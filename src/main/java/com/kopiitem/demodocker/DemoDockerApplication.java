package com.kopiitem.demodocker;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CacheControlHandlerInterceptor;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.filter.ShallowEtagHeaderFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.servlet.Filter;
import javax.transaction.Transactional;
import java.util.Optional;
import java.util.stream.Stream;

@SpringBootApplication
public class DemoDockerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoDockerApplication.class, args);
    }

}

@Configuration
class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new CacheControlHandlerInterceptor());
    }
}

@RestController
@RequestMapping("/user")
class userController {

    private final UserService userService;
    private final UserRepository userRepository;

    userController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @GetMapping("/{id}")
    @CacheControl(maxAge = 300, policy = {CachePolicy.MUST_REVALIDATE, CachePolicy.PRIVATE})
    public ResponseEntity<User> getUserById(@PathVariable("id") Long id) {

        return ResponseEntity.ok()
                //.cacheControl(CacheControl.maxAge(10, TimeUnit.SECONDS))
                .body(userService.getUserById(id).orElseThrow(userController.UserNotFoundException::new));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<User> updateUserById(@PathVariable("id") Long id, @RequestBody User newUser) {
        return ResponseEntity.ok()
                .body(userService.updateUserById(id, newUser).get());
    }

    @ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "User Not Found")
    static class UserNotFoundException extends RuntimeException {
    }
}

@Component
class CL implements CommandLineRunner {

    @Autowired
    UserRepository userRepository;

    @Bean
    public Filter shallowEtagHeaderFilter() {
        return new ShallowEtagHeaderFilter();
    }

    @Override
    public void run(String... args) throws Exception {
        Stream.of("Donny", "Wice", "Kensei")
                .map(User::new)
                .forEach(userRepository::save);
    }
}

@Service
@Transactional
class UserService {

    private final UserRepository userRepository;

    UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> updateUserById(Long id, User newUser) {
        return userRepository.findById(id).map(u -> {
            u.setName(newUser.getName());
            return u;
        });

    }


}


@Repository
interface UserRepository extends JpaRepository<User, Long> {

}

@Entity
@Getter
@Setter
@NoArgsConstructor
class User {
    @Id
    @GeneratedValue
    Long id;
    private String name;

    public User(String name) {
        this.name = name;
    }
}