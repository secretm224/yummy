package com.cho_co_song_i.yummy.yummy.entity;

import com.cho_co_song_i.yummy.yummy.dto.store.PhotoStatusConverter;
import com.cho_co_song_i.yummy.yummy.enums.PhotoStatus;
import jakarta.persistence.*;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;

@Entity
@Table(name = "review_photos")
public class ReviewPhotos implements Persistable<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "photo_id", nullable = false)
    private Long photoId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "review_id", nullable = false)
    private Reviews review;

    @Column(name = "object_key", nullable = false, length = 512)
    private String objectKey;

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    @Column(name = "bytes_size", nullable = false)
    private Long bytesSize;

    @Column(name = "width_px")
    private Integer widthPx;

    @Column(name = "height_px")
    private Integer heightPx;

    @Column(name = "checksum_sha256", nullable = false, length = 64)
    private String checksumSha256;

    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Convert(converter = PhotoStatusConverter.class)
    @Column(name = "status", nullable = false)
    private PhotoStatus status = PhotoStatus.PUBLISHED;

    @Column(name = "reg_dt", nullable = false)
    private LocalDateTime regDt;

    @Column(name = "chg_dt")
    private LocalDateTime chgDt;

    @Column(name = "reg_id", nullable = false, length = 25)
    private String regId;

    @Column(name = "chg_id", length = 25)
    private String chgId;

    /* ✅ Hibernate 에게 신규 엔티티임을 알려주기 위해 사용 */
    @Transient
    private boolean isNew = false;

    @Override
    public Long getId() { return this.photoId; }

    @Override
    public boolean isNew() { return isNew || this.photoId == null; }

    /* ✅ 새로운 엔티티를 표시하는 메서드 */
    public void markAsNew() { this.isNew = true; }
}
