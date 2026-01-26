package com.wallet.secure.service;

import com.wallet.secure.dto.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private ApiClientService apiClientService;

    public void registerUser(User user) throws Exception {
        // Delegate to API
        // UserDTO is passed, password should be plaintext, API encrypts it.
        apiClientService.registerUser(user);
    }

    public boolean verifyUser(String code) {
        return apiClientService.verifyUser(code);
    }
    
    public User findUserByEmail(String email) {
        return apiClientService.getUserByEmail(email);
    }

    public void initiatePasswordRecovery(String email) throws Exception {
        apiClientService.initiatePasswordRecovery(email);
    }

    public void resetPassword(String token, String newPassword) throws Exception {
        apiClientService.resetPassword(token, newPassword);
    }

    public void requestRoleChange(String email, String desiredRole) throws Exception {
        // API task?
    }

    public void approveRoleChange(String token) throws Exception {
        // API task?
    }

    public void rejectRoleChange(String token) throws Exception {
        // API task?
    }
}