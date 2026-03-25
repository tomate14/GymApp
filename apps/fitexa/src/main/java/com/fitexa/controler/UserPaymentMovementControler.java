package com.fitexa.controler;

import com.fitexa.dtos.UserPaymentMovementResponseDto;
import com.fitexa.services.UserPaymentMovementService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/{userId}/payments")
@RequiredArgsConstructor
@Tag(name = "Movimientos de Pago", description = "Consulta de movimientos de pago por alumno.")
public class UserPaymentMovementControler {

    private final UserPaymentMovementService userPaymentMovementService;

    @GetMapping
    public List<UserPaymentMovementResponseDto> findByUserId(@PathVariable Long userId) {
        return userPaymentMovementService.findByUserId(userId);
    }
}
