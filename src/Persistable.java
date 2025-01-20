import java.util.function.Function;

public interface Persistable {
    // functie abstracta ca sa obtinem id
    int getId();

    // Metode default pentru serializarea si deserializarea obiectelor
    default String toCSVString() {
        throw new UnsupportedOperationException("Serialization not implemented.");
    }

    // Metoda statica pentru crearea unei instante dintr-un rând CSV
    static <T extends Persistable> T fromCSVString(String[] csvData, Function<String[], T> constructor) {
        return constructor.apply(csvData);
    }
}
