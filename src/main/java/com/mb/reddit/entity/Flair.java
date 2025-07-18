package com.mb.reddit.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "flairs")
@Data
@NoArgsConstructor
public class Flair {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String label;

    private String color;

    @ManyToOne
    @JoinColumn(name = "community_id")
    private Community community;
}