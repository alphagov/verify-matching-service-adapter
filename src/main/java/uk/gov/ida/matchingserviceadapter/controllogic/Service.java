package uk.gov.ida.matchingserviceadapter.controllogic;

@FunctionalInterface
interface Service<T, U> {

    U apply(T u);
}
