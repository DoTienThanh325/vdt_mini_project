package com.vdt.documenttransfer.modules.role.entity;

import com.vdt.documenttransfer.modules.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "roles")
public class Role {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "role_code", nullable = false, unique = true, length = 100)
	private String roleCode;

	@Column(name = "role_name", nullable = false)
	private String roleName;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Builder.Default
	@OneToMany(mappedBy = "role")
	private Set<User> users = new HashSet<>();
}
