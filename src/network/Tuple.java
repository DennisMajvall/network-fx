package network;

public class Tuple<LeftClass, RightClass> {
    public final LeftClass left;
    public final RightClass right;

    public Tuple(LeftClass left, RightClass right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public int hashCode() {
        return left.hashCode() + right.hashCode();
    }

    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) { return true; }
        if (obj == null) { return false; }
        if (getClass() != obj.getClass()) { return false; }

        final Tuple other = (Tuple) obj;
        return left.equals(other.left) && right.equals(other.right);
    }
}
