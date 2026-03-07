package com.fitexa.controler;

import com.fitexa.dtos.UserRequestDto;
import com.fitexa.dtos.UserResponseDto;
import com.fitexa.dtos.DashboardPageResponseDto;
import com.fitexa.services.UserService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserControler {

    private final UserService userService;

    @GetMapping
    public List<UserResponseDto> findAll(@RequestParam(name = "type", required = false) String type) {
        if ("alumno".equalsIgnoreCase(type)) {
            return userService.findStudents();
        }
        if ("profesor".equalsIgnoreCase(type)) {
            return userService.findTeachers();
        }
        return userService.findAll();
    }

    @GetMapping("/page")
    public DashboardPageResponseDto<UserResponseDto> findPage(
        @RequestParam(name = "type", required = false) String type,
        @RequestParam(name = "name", required = false) String name,
        @RequestParam(name = "dni", required = false) String dni,
        @RequestParam(name = "paymentStatus", required = false) String paymentStatus,
        @RequestParam(name = "registrationFrom", required = false) LocalDate registrationFrom,
        @RequestParam(name = "registrationTo", required = false) LocalDate registrationTo,
        @RequestParam(name = "renewalFrom", required = false) LocalDate renewalFrom,
        @RequestParam(name = "renewalTo", required = false) LocalDate renewalTo,
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "8") int size
    ) {
        return userService.findPage(
            type,
            name,
            dni,
            paymentStatus,
            registrationFrom,
            registrationTo,
            renewalFrom,
            renewalTo,
            page,
            size
        );
    }

    @GetMapping("/{id}")
    public UserResponseDto findById(@PathVariable Long id) {
        return userService.findById(id);
    }

    @PostMapping
    public UserResponseDto create(@Valid @RequestBody UserRequestDto request) {
        return userService.create(request);
    }

    @PutMapping("/{id}")
    public UserResponseDto update(@PathVariable Long id, @Valid @RequestBody UserRequestDto request) {
        return userService.update(id, request);
    }

    @PostMapping("/{id}/renew")
    public UserResponseDto renew(@PathVariable Long id) {
        return userService.renewQuota(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        userService.delete(id);
    }
}
