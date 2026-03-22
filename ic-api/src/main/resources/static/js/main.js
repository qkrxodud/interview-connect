// Interview Connect - Main JavaScript

document.addEventListener('DOMContentLoaded', function() {
    // 초기화 함수들 실행
    initializeComponents();
    setupEventListeners();
    enhanceUserExperience();
});

/**
 * 컴포넌트 초기화
 */
function initializeComponents() {
    // 툴팁 초기화 (Bootstrap 5)
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(function(tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });

    // 팝오버 초기화
    const popoverTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="popover"]'));
    popoverTriggerList.map(function(popoverTriggerEl) {
        return new bootstrap.Popover(popoverTriggerEl);
    });

    // 알림 메시지 자동 숨김 (5초 후)
    const alerts = document.querySelectorAll('.alert');
    alerts.forEach(function(alert) {
        if (alert.classList.contains('alert-dismissible')) {
            setTimeout(function() {
                const alertInstance = bootstrap.Alert.getOrCreateInstance(alert);
                alertInstance.close();
            }, 5000);
        }
    });
}

/**
 * 이벤트 리스너 설정
 */
function setupEventListeners() {
    // 검색 폼 개선
    enhanceSearchForm();

    // 폼 검증 개선
    enhanceFormValidation();

    // 카드 클릭 이벤트
    setupCardClickEvents();

    // 무한 스크롤 (추후 구현 가능)
    // setupInfiniteScroll();
}

/**
 * 검색 폼 개선
 */
function enhanceSearchForm() {
    const searchForm = document.querySelector('form[action*="/reviews"]');
    if (!searchForm) return;

    // 검색 조건 변경 시 자동 검색 (디바운스 적용)
    const formInputs = searchForm.querySelectorAll('select, input[type="text"]');
    let searchTimeout;

    formInputs.forEach(function(input) {
        input.addEventListener('input', function() {
            clearTimeout(searchTimeout);
            searchTimeout = setTimeout(function() {
                // 자동 검색은 사용자 경험에 따라 선택적으로 활성화
                // searchForm.submit();
            }, 500);
        });
    });

    // 검색 버튼 로딩 상태
    const submitBtn = searchForm.querySelector('button[type="submit"]');
    if (submitBtn) {
        searchForm.addEventListener('submit', function() {
            submitBtn.disabled = true;
            submitBtn.innerHTML = '<span class="loading"></span> 검색 중...';
        });
    }
}

/**
 * 폼 검증 개선
 */
function enhanceFormValidation() {
    // Bootstrap 5 폼 검증
    const forms = document.querySelectorAll('.needs-validation');
    forms.forEach(function(form) {
        form.addEventListener('submit', function(event) {
            if (!form.checkValidity()) {
                event.preventDefault();
                event.stopPropagation();
            }
            form.classList.add('was-validated');
        }, false);
    });

    // 실시간 검증
    const inputs = document.querySelectorAll('input, textarea, select');
    inputs.forEach(function(input) {
        input.addEventListener('blur', function() {
            validateField(this);
        });

        input.addEventListener('input', function() {
            if (this.classList.contains('is-invalid')) {
                validateField(this);
            }
        });
    });
}

/**
 * 필드 검증
 */
function validateField(field) {
    const isValid = field.checkValidity();
    const feedback = field.parentElement.querySelector('.invalid-feedback');

    if (isValid) {
        field.classList.remove('is-invalid');
        field.classList.add('is-valid');
    } else {
        field.classList.remove('is-valid');
        field.classList.add('is-invalid');

        if (feedback) {
            feedback.textContent = getValidationMessage(field);
        }
    }

    return isValid;
}

/**
 * 검증 메시지 생성
 */
function getValidationMessage(field) {
    const validity = field.validity;

    if (validity.valueMissing) {
        return '이 필드는 필수입니다.';
    }
    if (validity.typeMismatch) {
        if (field.type === 'email') {
            return '올바른 이메일 주소를 입력해주세요.';
        }
    }
    if (validity.tooShort) {
        return `최소 ${field.minLength}자 이상 입력해주세요.`;
    }
    if (validity.tooLong) {
        return `최대 ${field.maxLength}자까지 입력 가능합니다.`;
    }
    if (validity.patternMismatch) {
        return '올바른 형식으로 입력해주세요.';
    }

    return field.validationMessage;
}

/**
 * 카드 클릭 이벤트 설정
 */
function setupCardClickEvents() {
    const reviewCards = document.querySelectorAll('.card[data-href]');
    reviewCards.forEach(function(card) {
        card.style.cursor = 'pointer';
        card.addEventListener('click', function(e) {
            // 버튼이나 링크 클릭 시에는 카드 클릭 이벤트 무시
            if (e.target.closest('a, button')) {
                return;
            }

            const href = this.dataset.href;
            if (href) {
                window.location.href = href;
            }
        });
    });
}

/**
 * 사용자 경험 개선
 */
function enhanceUserExperience() {
    // 페이지 로딩 애니메이션
    addLoadingAnimations();

    // 스크롤 위치 복원
    restoreScrollPosition();

    // 외부 링크 처리
    handleExternalLinks();

    // 이미지 지연 로딩
    setupLazyLoading();
}

/**
 * 로딩 애니메이션 추가
 */
function addLoadingAnimations() {
    const cards = document.querySelectorAll('.card');
    cards.forEach(function(card, index) {
        card.style.animationDelay = (index * 0.1) + 's';
        card.classList.add('fade-in');
    });
}

/**
 * 스크롤 위치 복원
 */
function restoreScrollPosition() {
    if (sessionStorage.getItem('scrollPosition')) {
        window.scrollTo(0, sessionStorage.getItem('scrollPosition'));
        sessionStorage.removeItem('scrollPosition');
    }

    // 페이지 이동 시 스크롤 위치 저장
    window.addEventListener('beforeunload', function() {
        sessionStorage.setItem('scrollPosition', window.scrollY);
    });
}

/**
 * 외부 링크 처리
 */
function handleExternalLinks() {
    const externalLinks = document.querySelectorAll('a[href^="http"]');
    externalLinks.forEach(function(link) {
        if (link.hostname !== window.location.hostname) {
            link.target = '_blank';
            link.rel = 'noopener noreferrer';

            // 외부 링크 아이콘 추가
            if (!link.querySelector('.fa-external-link-alt')) {
                link.innerHTML += ' <i class="fas fa-external-link-alt fa-sm"></i>';
            }
        }
    });
}

/**
 * 이미지 지연 로딩 설정
 */
function setupLazyLoading() {
    const images = document.querySelectorAll('img[data-src]');

    if ('IntersectionObserver' in window) {
        const imageObserver = new IntersectionObserver(function(entries, observer) {
            entries.forEach(function(entry) {
                if (entry.isIntersecting) {
                    const img = entry.target;
                    img.src = img.dataset.src;
                    img.removeAttribute('data-src');
                    imageObserver.unobserve(img);
                }
            });
        });

        images.forEach(function(img) {
            imageObserver.observe(img);
        });
    } else {
        // Intersection Observer를 지원하지 않는 경우 즉시 로드
        images.forEach(function(img) {
            img.src = img.dataset.src;
            img.removeAttribute('data-src');
        });
    }
}

/**
 * API 호출 유틸리티
 */
const API = {
    baseUrl: '/api/v1',

    async request(endpoint, options = {}) {
        const url = this.baseUrl + endpoint;
        const config = {
            headers: {
                'Content-Type': 'application/json',
                ...options.headers
            },
            ...options
        };

        try {
            const response = await fetch(url, config);
            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || '요청 처리 중 오류가 발생했습니다.');
            }

            return data;
        } catch (error) {
            console.error('API 요청 오류:', error);
            throw error;
        }
    },

    get(endpoint) {
        return this.request(endpoint);
    },

    post(endpoint, data) {
        return this.request(endpoint, {
            method: 'POST',
            body: JSON.stringify(data)
        });
    },

    put(endpoint, data) {
        return this.request(endpoint, {
            method: 'PUT',
            body: JSON.stringify(data)
        });
    },

    delete(endpoint) {
        return this.request(endpoint, {
            method: 'DELETE'
        });
    }
};

/**
 * 알림 표시 유틸리티
 */
function showAlert(message, type = 'info') {
    const alertContainer = document.querySelector('.container .alert') || document.querySelector('.container');
    if (!alertContainer) return;

    const alertElement = document.createElement('div');
    alertElement.className = `alert alert-${type} alert-dismissible fade show`;
    alertElement.innerHTML = `
        <i class="fas fa-${getAlertIcon(type)}"></i>
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;

    alertContainer.insertBefore(alertElement, alertContainer.firstChild);

    // 5초 후 자동 제거
    setTimeout(() => {
        const alertInstance = bootstrap.Alert.getOrCreateInstance(alertElement);
        alertInstance.close();
    }, 5000);
}

/**
 * 알림 아이콘 반환
 */
function getAlertIcon(type) {
    const icons = {
        success: 'check-circle',
        danger: 'exclamation-circle',
        warning: 'exclamation-triangle',
        info: 'info-circle'
    };
    return icons[type] || 'info-circle';
}

/**
 * 로딩 상태 표시
 */
function showLoading(element, text = '로딩 중...') {
    const originalContent = element.innerHTML;
    element.dataset.originalContent = originalContent;
    element.innerHTML = `<span class="loading"></span> ${text}`;
    element.disabled = true;
}

/**
 * 로딩 상태 해제
 */
function hideLoading(element) {
    if (element.dataset.originalContent) {
        element.innerHTML = element.dataset.originalContent;
        delete element.dataset.originalContent;
    }
    element.disabled = false;
}

// 전역 함수로 노출 (다른 스크립트에서 사용 가능)
window.InterviewConnect = {
    API,
    showAlert,
    showLoading,
    hideLoading,
    validateField
};