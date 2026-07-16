package com.hospital.reservation.inquiry;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InquiryServiceTest {

    @Test
    void returnsPagedItemsAndOverallStatusCounts() {
        InquiryRepository repository = mock(InquiryRepository.class);
        Inquiry inquiry = Inquiry.create("홍길동", "01012345678", "test@example.com", "문의 내용");
        when(repository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(inquiry), Pageable.ofSize(20), 21));
        when(repository.countByStatus(InquiryStatus.RECEIVED)).thenReturn(12L);
        when(repository.countByStatus(InquiryStatus.IN_PROGRESS)).thenReturn(5L);
        when(repository.countByStatus(InquiryStatus.ANSWERED)).thenReturn(4L);

        InquiryPageResponse result = new InquiryService(repository).findAll(0, 20);

        assertThat(result.items()).hasSize(1);
        assertThat(result.totalElements()).isEqualTo(21);
        assertThat(result.totalPages()).isEqualTo(2);
        assertThat(result.receivedCount()).isEqualTo(12);
        assertThat(result.inProgressCount()).isEqualTo(5);
        assertThat(result.answeredCount()).isEqualTo(4);
        verify(repository).findAll(any(Pageable.class));
    }
}
