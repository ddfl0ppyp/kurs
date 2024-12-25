package kurs;

class Pair {
    private String key;
    private int value;

    public Pair(String key, int value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public int getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true; 
        if (!(obj instanceof Pair)) return false; 
        Pair pair = (Pair) obj;
        return this.key.equals(pair.key) && this.value == pair.value; 
    }

    @Override
    public int hashCode() {
        int result = key.hashCode(); 
        result = 31 * result + value; 
        return result;
    }
}
