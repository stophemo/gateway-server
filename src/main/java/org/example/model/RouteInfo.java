package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: huojie
 * @date: 2024/01/30 20:52
 **/
@AllArgsConstructor
@NoArgsConstructor
@Data
public class RouteInfo {
    private String path;
    private String targetAddress;
}
