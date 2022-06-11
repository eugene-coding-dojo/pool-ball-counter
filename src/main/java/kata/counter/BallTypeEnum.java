package kata.counter;

import java.util.EnumSet;
import java.util.Set;
import lombok.ToString;

@ToString
public enum BallTypeEnum {
    CUE(0),
    SOLID_YELLOW(1),
    SOLID_BLUE(2),
    SOLID_RED(3),
    SOLID_PURPLE(4),
    SOLID_ORANGE(5),
    SOLID_GREEN(6),
    SOLID_MAROON(7),

    EIGHT(8),

    STRIPED_YELLOW(9),
    STRIPED_BLUE(10),
    STRIPED_RED(11),
    STRIPED_PURPLE(12),
    STRIPED_ORANGE(13),
    STRIPED_GREEN(14),
    STRIPED_MAROON(15),

    // Undefined
    UNDEFINED(-1);

    private final int id;

    BallTypeEnum(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    /**
     * Map id to enum.
     *
     * @param id enum identifier
     * @return enum
     */
    public static BallTypeEnum getById(long id) {
        for (BallTypeEnum e : values()) {
            if (e.getId() == id) {
                return e;
            }
        }
        return UNDEFINED;
    }

    public static final Set<BallTypeEnum> CUE_BALLS = EnumSet.of(CUE);
    public static final Set<BallTypeEnum> EIGHT_BALLS = EnumSet.of(EIGHT);
    public static final Set<BallTypeEnum> SOLID_BALLS = EnumSet.of(SOLID_YELLOW, SOLID_BLUE, SOLID_RED,
            SOLID_PURPLE, SOLID_ORANGE, SOLID_GREEN, SOLID_MAROON);
    public static final Set<BallTypeEnum> STRIPED_BALLS = EnumSet.of(STRIPED_YELLOW, STRIPED_BLUE, STRIPED_RED,
            STRIPED_PURPLE, STRIPED_ORANGE, STRIPED_GREEN, STRIPED_MAROON);

}
