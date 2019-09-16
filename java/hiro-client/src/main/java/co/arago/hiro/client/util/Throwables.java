package co.arago.hiro.client.util;

public final class Throwables {

  private Throwables() {
  }

  public static <T> T unchecked(final Throwable e) {
    Throwables.<RuntimeException>throwAny(e);

    return null;
  }

  @SuppressWarnings("unchecked")
  private static <E extends Throwable> void throwAny(final Throwable e) throws E {
    throw (E) e;
  }
}
