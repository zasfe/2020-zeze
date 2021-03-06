package dev.minguinho.zeze.domain.slide.documentation;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.minguinho.zeze.domain.auth.api.dto.response.AuthenticationDto;
import dev.minguinho.zeze.domain.auth.infra.AuthorizationTokenExtractor;
import dev.minguinho.zeze.domain.auth.infra.JwtTokenProvider;
import dev.minguinho.zeze.domain.documentation.Documentation;
import dev.minguinho.zeze.domain.slide.api.SlideController;
import dev.minguinho.zeze.domain.slide.api.dto.SlideMetadataDto;
import dev.minguinho.zeze.domain.slide.api.dto.SlideMetadataDtos;
import dev.minguinho.zeze.domain.slide.api.dto.SlideRequestDto;
import dev.minguinho.zeze.domain.slide.api.dto.SlideResponseDto;
import dev.minguinho.zeze.domain.slide.service.SlideService;
import dev.minguinho.zeze.domain.user.config.LoginUserIdMethodArgumentResolver;

@WebMvcTest(controllers = {SlideController.class})
public class SlideDocumentation extends Documentation {
    private static final String BASE_URL = "/api/slides/";

    @MockBean
    private SlideService slideService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private AuthorizationTokenExtractor authorizationTokenExtractor;

    @MockBean
    private LoginUserIdMethodArgumentResolver loginUserIdMethodArgumentResolver;

    @Autowired
    private ObjectMapper objectMapper;

    private AuthenticationDto authenticationDto;

    @BeforeEach
    public void setUp(WebApplicationContext context, RestDocumentationContextProvider restDocumentation) {
        super.setUp(context, restDocumentation);
        authenticationDto = AuthenticationDto.builder().accessToken("token").build();
    }

    @Test
    void createSlide() throws Exception {
        SlideRequestDto slideRequestDto = new SlideRequestDto("제목", "부제목", "작성자", "2020-07-21", "내용", "PUBLIC");
        given(slideService.create(any(), anyLong())).willReturn(1L);
        given(authorizationTokenExtractor.extract(any(), any())).willReturn("");
        given(loginUserIdMethodArgumentResolver.supportsParameter(any())).willReturn(true);
        given(loginUserIdMethodArgumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(1L);
        String content = objectMapper.writeValueAsString(slideRequestDto);

        mockMvc.perform(post(BASE_URL)
            .header("Authorization", "bearer " + authenticationDto.getAccessToken())
            .contentType(MediaType.APPLICATION_JSON)
            .content(content)
        )
            .andExpect(status().isCreated())
            .andDo(print())
            .andDo(document("slides/create",
                getDocumentRequest(),
                getDocumentResponse(),
                requestHeaders(
                    headerWithName("Authorization").description("Bearer auth credentials")
                ),
                requestFields(
                    fieldWithPath("title").type(JsonFieldType.STRING).description("제목"),
                    fieldWithPath("subtitle").type(JsonFieldType.STRING).description("부제목"),
                    fieldWithPath("author").type(JsonFieldType.STRING).description("작성자"),
                    fieldWithPath("presentedAt").type(JsonFieldType.STRING).description("발표 날짜"),
                    fieldWithPath("content").type(JsonFieldType.STRING).description("내용"),
                    fieldWithPath("accessLevel").type(JsonFieldType.STRING).description("접근 레벨")
                ),
                responseHeaders(
                    headerWithName("Location").description("생성된 슬라이드 URI")
                ))
            );
    }

    @Test
    void retrieveSlides() throws Exception {
        List<SlideMetadataDto> slides = Collections.singletonList(
            new SlideMetadataDto(1L, "제목", "부제목", "작성자", "2020-07-21", ZonedDateTime.now(), ZonedDateTime.now())
        );
        SlideMetadataDtos slideMetadataDtos = new SlideMetadataDtos(slides, 0);
        given(slideService.retrieveAll(any(), eq(null))).willReturn(slideMetadataDtos);
        given(authorizationTokenExtractor.extract(any(), any())).willReturn("");
        given(loginUserIdMethodArgumentResolver.supportsParameter(any())).willReturn(true);
        given(loginUserIdMethodArgumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(null);

        mockMvc.perform(get(BASE_URL)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .param("page", "0")
            .param("size", "5")
        )
            .andExpect(status().isOk())
            .andDo(print())
            .andDo(document("slides/retrieveAllPublic",
                getDocumentRequest(),
                getDocumentResponse(),
                requestParameters(
                    parameterWithName("page").description("페이지 번호"),
                    parameterWithName("size").description("슬라이드 개수")
                ),
                responseFields(
                    fieldWithPath("slides").type(JsonFieldType.ARRAY).description("Public 슬라이드 목록"),
                    fieldWithPath("totalPage").type(JsonFieldType.NUMBER).description("총 페이지 수"),
                    fieldWithPath("slides[0].id").type(JsonFieldType.NUMBER).description("슬라이드 id"),
                    fieldWithPath("slides[0].title").type(JsonFieldType.STRING).description("슬라이드 제목"),
                    fieldWithPath("slides[0].subtitle").type(JsonFieldType.STRING).description("슬라이드 부제목"),
                    fieldWithPath("slides[0].author").type(JsonFieldType.STRING).description("슬라이드 작성자"),
                    fieldWithPath("slides[0].presentedAt").type(JsonFieldType.STRING).description("슬라이드 발표 날짜"),
                    fieldWithPath("slides[0].createdAt").type(JsonFieldType.STRING).description("슬라이드 생성 날짜"),
                    fieldWithPath("slides[0].updatedAt").type(JsonFieldType.STRING).description("슬라이드 수정 날짜")))
            );
    }

    @Test
    void retrieveMySlides() throws Exception {
        List<SlideMetadataDto> slides = Collections.singletonList(
            new SlideMetadataDto(1L, "제목", "부제목", "작성자", "2020-07-21", ZonedDateTime.now(),
                ZonedDateTime.now())
        );
        SlideMetadataDtos slideMetadataDtos = new SlideMetadataDtos(slides, 0);
        given(slideService.retrieveAll(any(), anyLong())).willReturn(slideMetadataDtos);
        given(authorizationTokenExtractor.extract(any(), any())).willReturn("");
        given(loginUserIdMethodArgumentResolver.supportsParameter(any())).willReturn(true);
        given(loginUserIdMethodArgumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(1L);

        mockMvc.perform(get(BASE_URL)
            .header("Authorization", "bearer " + authenticationDto.getAccessToken())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .param("page", "0")
            .param("size", "5")
        )
            .andExpect(status().isOk())
            .andDo(print())
            .andDo(document("slides/retrieveAll",
                getDocumentRequest(),
                getDocumentResponse(),
                requestHeaders(
                    headerWithName("Authorization").description("Bearer auth credentials")
                ),
                requestParameters(
                    parameterWithName("page").description("페이지 번호"),
                    parameterWithName("size").description("슬라이드 개수")
                ),
                responseFields(
                    fieldWithPath("slides").type(JsonFieldType.ARRAY).description("User 슬라이드 목록"),
                    fieldWithPath("totalPage").type(JsonFieldType.NUMBER).description("총 페이지 수"),
                    fieldWithPath("slides[0].id").type(JsonFieldType.NUMBER).description("슬라이드 id"),
                    fieldWithPath("slides[0].title").type(JsonFieldType.STRING).description("슬라이드 제목"),
                    fieldWithPath("slides[0].subtitle").type(JsonFieldType.STRING).description("슬라이드 부제목"),
                    fieldWithPath("slides[0].author").type(JsonFieldType.STRING).description("슬라이드 작성자"),
                    fieldWithPath("slides[0].presentedAt").type(JsonFieldType.STRING).description("슬라이드 발표 날짜"),
                    fieldWithPath("slides[0].createdAt").type(JsonFieldType.STRING).description("슬라이드 생성 날짜"),
                    fieldWithPath("slides[0].updatedAt").type(JsonFieldType.STRING).description("슬라이드 수정 날짜")))
            );
    }

    @Test
    void retrieveSlide() throws Exception {
        SlideResponseDto slideResponseDto = new SlideResponseDto("내용", "PUBLIC", ZonedDateTime.now());
        given(slideService.retrieve(anyLong(), eq(null))).willReturn(slideResponseDto);
        given(authorizationTokenExtractor.extract(any(), any())).willReturn("");
        given(loginUserIdMethodArgumentResolver.supportsParameter(any())).willReturn(true);
        given(loginUserIdMethodArgumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(null);

        mockMvc.perform(get(BASE_URL + "{id}", 1)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
        )
            .andExpect(status().isOk())
            .andDo(print())
            .andDo(document("slides/retrievePublic",
                getDocumentRequest(),
                getDocumentResponse(),
                pathParameters(
                    parameterWithName("id").description("슬라이드 id")
                ),
                responseFields(
                    fieldWithPath("content").type(JsonFieldType.STRING).description("슬라이드 내용"),
                    fieldWithPath("accessLevel").type(JsonFieldType.STRING).description("슬라이드 접근 권한"),
                    fieldWithPath("updatedAt").type(JsonFieldType.STRING).description("슬라이드 수정 날짜")))
            );
    }

    @Test
    void retrieveMySlide() throws Exception {
        SlideResponseDto slideResponseDto = new SlideResponseDto("내용", "PRIVATE", ZonedDateTime.now());
        given(slideService.retrieve(anyLong(), anyLong())).willReturn(slideResponseDto);
        given(authorizationTokenExtractor.extract(any(), any())).willReturn("");
        given(loginUserIdMethodArgumentResolver.supportsParameter(any())).willReturn(true);
        given(loginUserIdMethodArgumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(1L);

        mockMvc.perform(get(BASE_URL + "{id}", 1)
            .header("Authorization", "bearer " + authenticationDto.getAccessToken())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
        )
            .andExpect(status().isOk())
            .andDo(print())
            .andDo(document("slides/retrieve",
                getDocumentRequest(),
                getDocumentResponse(),
                requestHeaders(
                    headerWithName("Authorization").description("Bearer auth credentials")
                ),
                pathParameters(
                    parameterWithName("id").description("슬라이드 id")
                ),
                responseFields(
                    fieldWithPath("content").type(JsonFieldType.STRING).description("슬라이드 내용"),
                    fieldWithPath("accessLevel").type(JsonFieldType.STRING).description("슬라이드 접근 권한"),
                    fieldWithPath("updatedAt").type(JsonFieldType.STRING).description("슬라이드 수정 날짜")))
            );
    }

    @Test
    void updateSlide() throws Exception {
        SlideRequestDto updateRequestDto = new SlideRequestDto("새 제목", "새 부제목", "새 저자", "2020-07-22", "새 내용",
            "PRIVATE");
        given(authorizationTokenExtractor.extract(any(), any())).willReturn("");
        given(loginUserIdMethodArgumentResolver.supportsParameter(any())).willReturn(true);
        given(loginUserIdMethodArgumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(1L);
        String content = objectMapper.writeValueAsString(updateRequestDto);

        mockMvc.perform(patch(BASE_URL + "{id}", 1)
            .header("Authorization", "bearer " + authenticationDto.getAccessToken())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(content)
        )
            .andExpect(status().isNoContent())
            .andDo(print())
            .andDo(document("slides/update",
                getDocumentRequest(),
                getDocumentResponse(),
                requestHeaders(
                    headerWithName("Authorization").description("Bearer auth credentials")
                ),
                pathParameters(
                    parameterWithName("id").description("슬라이드 id")
                ),
                requestFields(
                    fieldWithPath("title").type(JsonFieldType.STRING).description("수정할 제목"),
                    fieldWithPath("subtitle").type(JsonFieldType.STRING).description("수정할 부제목"),
                    fieldWithPath("author").type(JsonFieldType.STRING).description("수정할 저자"),
                    fieldWithPath("presentedAt").type(JsonFieldType.STRING).description("수정할 발표 날짜"),
                    fieldWithPath("content").type(JsonFieldType.STRING).description("수정할 내용"),
                    fieldWithPath("accessLevel").type(JsonFieldType.STRING).description("수정할 접근 레벨")))
            );
    }

    @Test
    void deleteSlide() throws Exception {
        given(authorizationTokenExtractor.extract(any(), any())).willReturn("");
        given(loginUserIdMethodArgumentResolver.supportsParameter(any())).willReturn(true);
        given(loginUserIdMethodArgumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(1L);

        mockMvc.perform(delete(BASE_URL + "{id}", 1)
            .header("Authorization", "bearer " + authenticationDto.getAccessToken())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
        )
            .andExpect(status().isNoContent())
            .andDo(print())
            .andDo(document("slides/delete",
                getDocumentRequest(),
                getDocumentResponse(),
                pathParameters(
                    parameterWithName("id").description("슬라이드 id")
                ),
                requestHeaders(
                    headerWithName("Authorization").description("Bearer auth credentials")))
            );
    }

    @Test
    void cloneSlide() throws Exception {
        given(authorizationTokenExtractor.extract(any(), any())).willReturn("");
        given(loginUserIdMethodArgumentResolver.supportsParameter(any())).willReturn(true);
        given(loginUserIdMethodArgumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(1L);
        given(slideService.clone(anyLong(), anyLong())).willReturn(2L);

        mockMvc.perform(post(BASE_URL + "{id}", 1)
            .header("Authorization", "bearer " + authenticationDto.getAccessToken())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
        )
            .andExpect(status().isCreated())
            .andDo(print())
            .andDo(document("slides/clone",
                getDocumentRequest(),
                getDocumentResponse(),
                requestHeaders(
                    headerWithName("Authorization").description("Bearer auth credentials")
                ),
                pathParameters(
                    parameterWithName("id").description("슬라이드 id")
                ),
                responseHeaders(
                    headerWithName("Location").description("복제된 슬라이드 URI")
                ))
            );
    }
}
