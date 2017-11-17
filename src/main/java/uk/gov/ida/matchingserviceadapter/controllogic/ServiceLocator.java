package uk.gov.ida.matchingserviceadapter.controllogic;


/**
 * A broad functional interface for locating a service S, given some input context C.
 *
 * @param <C> the context under which a service is to be located.
 * @param <S> the type of service returned by this locator.
 */
@FunctionalInterface
public interface ServiceLocator<C, S> {
    S findServiceFor(C context);
}
