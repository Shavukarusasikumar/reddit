package com.mb.reddit.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "comment_likes", uniqueConstraints = {
		@UniqueConstraint(columnNames = {"user_id", "comment_id"})
})
public class CommentVote {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne
	@JoinColumn(name = "comment_id", nullable = false)
	private Comment comment;

	private Boolean isLike;

	private LocalDateTime reactedAt;
}