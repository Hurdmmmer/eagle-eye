package com.thoughtmechanix.licenses.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Organization implements Serializable {
    String id;
    String name;
    String contactName;
    String contactEmail;
    String contactPhone;
}