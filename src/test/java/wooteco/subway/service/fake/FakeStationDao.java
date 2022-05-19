package wooteco.subway.service.fake;

import static java.util.stream.Collectors.toList;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.util.ReflectionUtils;
import wooteco.subway.dao.StationDao;
import wooteco.subway.domain.Station;
import wooteco.subway.exception.NotExistException;

public class FakeStationDao implements StationDao {

    private static final int DELETE_SUCCESS = 1;

    private static Long seq = 0L;
    private final List<Station> stations = new ArrayList<>();

    @Override
    public Long save(Station station) {
        final List<String> names = getNaems();

        if (names.contains(station.getName())) {
            throw new DuplicateKeyException("동일한 station이 존재합니다.");
        }

        Station persistStation = createNewObject(station);
        stations.add(persistStation);
        return persistStation.getId();
    }

    @Override
    public Optional<Station> findById(Long id) {
        return stations.stream()
                .filter(station -> station.getId().equals(id))
                .findAny();
    }

    @Override
    public List<Station> findAll() {
        return List.copyOf(stations);
    }

    @Override
    public List<Station> findAllByIds(List<Long> stationIds) {
        return stationIds.stream().sequential()
                .map(id -> findById(id).orElseThrow(() -> new NotExistException("존재하지 않는 지하철 역입니다.")))
                .collect(toList());
    }

    @Override
    public int deleteById(Long id) {
        final Station findStation = stations.stream()
                .filter(station -> station.getId().equals(id))
                .findAny()
                .orElseThrow(IllegalArgumentException::new);

        stations.remove(findStation);
        return DELETE_SUCCESS;
    }

    private static Station createNewObject(Station station) {
        Field field = ReflectionUtils.findField(Station.class, "id");
        field.setAccessible(true);
        ReflectionUtils.setField(field, station, ++seq);
        return station;
    }

    private List<String> getNaems() {
        return stations.stream()
                .map(Station::getName)
                .collect(toList());
    }
}
