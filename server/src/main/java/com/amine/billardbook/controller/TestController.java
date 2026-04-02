package com.amine.billardbook.controller;

import com.amine.billardbook.dao.ReservationDao;
import com.amine.billardbook.dao.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Controller for test operations and data reset.
 */
@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private ReservationDao reservationDao;
    
    @Autowired
    private UserDao userDao;

    @DeleteMapping("/reset")
    public ResponseEntity<String> resetAllData() {
        try {
            // Clear reservations by reflection to access the private collection.
            Field reservationField = reservationDao.getClass().getSuperclass().getDeclaredField("collection");
            reservationField.setAccessible(true);
            List<?> reservationCollection = (List<?>) reservationField.get(reservationDao);
            reservationCollection.clear();
            
            // Clear users the same way.
            Field userField = userDao.getClass().getSuperclass().getDeclaredField("collection");
            userField.setAccessible(true);
            List<?> userCollection = (List<?>) userField.get(userDao);
            userCollection.clear();
            
            System.out.println("DEBUG: All data reset successfully");
            return ResponseEntity.ok("All data reset successfully");
        } catch (Exception e) {
            System.err.println("ERROR: Failed to reset data: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Failed to reset data: " + e.getMessage());
        }
    }
}
