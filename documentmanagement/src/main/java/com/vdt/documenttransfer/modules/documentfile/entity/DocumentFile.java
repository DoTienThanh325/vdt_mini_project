package com.vdt.documenttransfer.modules.documentfile.entity;

import com.vdt.documenttransfer.modules.document.entity.Document;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
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
@Table(name = "document_files")
public class DocumentFile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "original_file_name", nullable = false)
	private String originalFileName;

	@Column(name = "stored_file_name", nullable = false, unique = true)
	private String storedFileName;

	@Column(name = "file_path", nullable = false, unique = true, length = 500)
	private String filePath;

	@Column(name = "file_type", nullable = false, length = 100)
	private String fileType;

	@Column(name = "file_size", nullable = false)
	private Long fileSize;

	@ManyToOne(optional = false)
	@JoinColumn(name = "document_id", nullable = false)
	private Document document;

	@Column(name = "uploaded_at", nullable = false, updatable = false)
	private LocalDateTime uploadedAt;
}
