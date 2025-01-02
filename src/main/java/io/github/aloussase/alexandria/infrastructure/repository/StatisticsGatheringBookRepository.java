package io.github.aloussase.alexandria.infrastructure.repository;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.City;
import io.github.aloussase.alexandria.domain.models.Book;
import io.github.aloussase.alexandria.domain.repository.BookRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.ResourceUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Optional;

@Slf4j
public class StatisticsGatheringBookRepository implements BookRepository {
    private final JdbcTemplate template;
    private final BookRepository inner;

    public StatisticsGatheringBookRepository(JdbcTemplate template, BookRepository inner) {
        this.template = template;
        this.inner = inner;
    }

    @Override
    public List<Book> searchByTitle(String title) {
        final var books = inner.searchByTitle(title);
        try {
            final var sql = """
                    insert into search_statistics (title, ip, location)
                    values (?, ?, ?)""";

            final var ipAddr = getIpAddr();
            final var city = ipAddr.flatMap(this::getCity);

            template.update(sql, title, ipAddr.map(InetAddress::getHostAddress).orElse(""),
                    city.orElse(null));
        } catch (Exception ex) {
            log.error("Failed to store search statistics", ex);
        }
        return books;
    }

    private Optional<String> getCity(InetAddress ipAddr) {
        try (final var reader = new DatabaseReader.Builder(
                ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + "GeoLite2-City.mmdb")).build()) {
            return reader
                    .tryCity(InetAddress.getByName(ipAddr.getHostName()))
                    .map(CityResponse::getCity)
                    .map(City::getName);
        } catch (IOException | GeoIp2Exception ignored) {
            return Optional.empty();
        }
    }

    private Optional<InetAddress> getIpAddr() {
        return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .filter(ServletRequestAttributes.class::isInstance)
                .map(ServletRequestAttributes.class::cast)
                .map(ServletRequestAttributes::getRequest)
                .map(HttpServletRequest::getRemoteAddr)
                .flatMap(addr -> {
                    try {
                        return Optional.of(InetAddress.getByName(addr));
                    } catch (UnknownHostException ignored) {
                        return Optional.empty();
                    }
                });
    }
}
