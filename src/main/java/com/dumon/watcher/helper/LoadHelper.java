package com.dumon.watcher.helper;

import com.dumon.watcher.entity.User;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

public class LoadHelper {
    /**
     * Open the *.json, unmarshal every entry into a Object.
     *
     * @return a List of User objects.
     * @throws IOException if ObjectMapper unable to open file.
     */
    public static List<User> importUsers() throws IOException {
        return new ObjectMapper().setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY).
                readValue(User.class.getResourceAsStream("/users.json"),new TypeReference<List<User>>(){});
    }
}
