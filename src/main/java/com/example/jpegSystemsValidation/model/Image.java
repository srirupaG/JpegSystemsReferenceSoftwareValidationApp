package com.example.jpegSystemsValidation.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "images")
public class Image {
	
	 @Id
	 @GeneratedValue(strategy = GenerationType.IDENTITY)
	 @Column(name = "image_id")
	 private Long id;
	 
	 @Column(name = "image_name")
	 private String imageName;
	 
	 @Column(name = "file_path")
	 private String filePath;
	 
	 @Column(name = "is_hidden")
	 private boolean isHidden;
	 
	 @ManyToOne
	 @JoinColumn(name = "user_id")
	 private User createdBy;
	 
	 
	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}


	public String getImageName() {
		return imageName;
	}


	public void setImageName(String imageName) {
		this.imageName = imageName;
	}


	public String getFilePath() {
		return filePath;
	}


	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}


	public User getCreatedBy() {
		return createdBy;
	}


	public void setCreatedBy(User createdBy) {
		this.createdBy = createdBy;
	}


	public boolean isHidden() {
		return isHidden;
	}


	public void setHidden(boolean isHidden) {
		this.isHidden = isHidden;
	}


}
