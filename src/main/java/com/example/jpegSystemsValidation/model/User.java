package com.example.jpegSystemsValidation.model;

import java.util.HashSet;
import java.util.Set;


import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {
	
	 @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    @Column(name = "user_id")
	    private Long id;

	    @Column(name = "username")
	    private String username;

	    @Column(name = "password")
	    private String password;
		
	 	@Column(name = "account_non_expired")
	    private boolean accountNonExpired;

	    @Column(name = "is_enabled")
	    private boolean isEnabled;

	    @Column(name = "account_non_locked")
	    private boolean accountNonLocked;

	    @Column(name = "credentials_non_expired")
	    private boolean credentialsNonExpired;
	    
		
	    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	    @JoinTable(name = "user_role", joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "user_id"),
	        inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "role_id"))
	    private Set<Role> roles;
	    
	    
	    @ManyToMany(fetch = FetchType.LAZY)
	    @JoinTable(name = "user_images", joinColumns = @JoinColumn(name = "user_id"),
	        inverseJoinColumns = @JoinColumn(name = "image_id"))
	    private Set<Image> images = new HashSet<>();
	    
	    
		public Long getId() {
			return id;
		}


		public void setId(Long id) {
			this.id = id;
		}


		public String getUsername() {
			return username;
		}


		public void setUsername(String username) {
			this.username = username;
		}
		

		public String getPassword() {
			return password;
		}


		public void setPassword(String password) {
			this.password = password;
		}


		public boolean isAccountNonExpired() {
			return accountNonExpired;
		}


		public void setAccountNonExpired(boolean accountNonExpired) {
			this.accountNonExpired = accountNonExpired;
		}


		public boolean isEnabled() {
			return isEnabled;
		}


		public void setEnabled(boolean isEnabled) {
			this.isEnabled = isEnabled;
		}


		public boolean isAccountNonLocked() {
			return accountNonLocked;
		}


		public void setAccountNonLocked(boolean accountNonLocked) {
			this.accountNonLocked = accountNonLocked;
		}


		public boolean isCredentialsNonExpired() {
			return credentialsNonExpired;
		}


		public void setCredentialsNonExpired(boolean credentialsNonExpired) {
			this.credentialsNonExpired = credentialsNonExpired;
		}


		public Set<Role> getRoles() {
			return roles;
		}


		public void setRoles(Set<Role> roles) {
			this.roles = roles;
		}


		public Set<Image> getImages() {
			return images;
		}


		public void setImages(Set<Image> images) {
			this.images = images;
		}
		
}
