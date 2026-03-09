package com.fitexa.services;

import com.fitexa.entities.UserEntity;

public interface AuthenticatedUserService {
    UserEntity getCurrentUser();
}
