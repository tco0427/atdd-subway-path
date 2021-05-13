package wooteco.subway.auth.application;

import io.jsonwebtoken.JwtException;
import org.springframework.stereotype.Service;
import wooteco.subway.auth.dto.TokenRequest;
import wooteco.subway.auth.dto.TokenResponse;
import wooteco.subway.auth.infrastructure.JwtTokenProvider;
import wooteco.subway.exception.InvalidMemberException;
import wooteco.subway.exception.InvalidTokenException;
import wooteco.subway.member.dao.MemberDao;
import wooteco.subway.member.domain.Member;
import wooteco.subway.member.domain.LoginMember;

@Service
public class AuthService {
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberDao memberDao;

    public AuthService(JwtTokenProvider jwtTokenProvider, MemberDao memberDao) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.memberDao = memberDao;
    }

    public TokenResponse createToken(TokenRequest tokenRequest) {
        Member member = findMemberEmailAndPassword(tokenRequest);
        return new TokenResponse(jwtTokenProvider.createToken(member.getId().toString()));
    }

    private Member findMemberEmailAndPassword(TokenRequest tokenRequest) {
        return memberDao.findByEmailAndPassword(tokenRequest.getEmail(), tokenRequest.getPassword())
                .orElseThrow(() -> new InvalidMemberException(tokenRequest.getEmail()));
    }

    public LoginMember findMemberByToken(String token) {
        try {
            String payload = jwtTokenProvider.getPayload(token);
            return new LoginMember(memberDao.findById(Long.valueOf(payload)));
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidTokenException();
        }
    }

    public void validateToken(String token) {
        jwtTokenProvider.validateToken(token);
    }
}
