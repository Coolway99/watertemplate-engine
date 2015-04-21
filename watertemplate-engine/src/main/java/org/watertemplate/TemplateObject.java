package org.watertemplate;

import org.watertemplate.exception.InvalidTemplateObjectEvaluationException;

import java.util.Collection;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Stream;

public interface TemplateObject {
    Stream<Supplier<String>> stream(final Locale locale);

    class LocaleSensitiveObject<T> implements TemplateObject {
        private final BiFunction<T, Locale, String> function;
        private final T object;

        protected LocaleSensitiveObject(final T object, final BiFunction<T, Locale, String> function) {
            this.function = function;
            this.object = object;
        }

        @Override
        public Stream<Supplier<String>> stream(final Locale locale) {
            return Stream.of(() -> function.apply(object, locale));
        }
    }

    public final class MappedObject<T> extends Mappable<T> implements TemplateObject {
        private final T object;

        MappedObject(final T object, final BiConsumer<T, TemplateMap.Arguments> mapper) {
            super(mapper);
            this.object = object;
        }

        public TemplateMap.Arguments map() {
            return map(object);
        }

        @Override
        public Stream<Supplier<String>> stream(final Locale locale) {
            if (object instanceof String) {
                return Stream.of(object::toString);
            } else {
                throw new InvalidTemplateObjectEvaluationException(
                        "MappedObjects should not be evaluated. " +
                                "If you're iterating, make sure your collection contains only Strings.");
            }
        }
    }

    public final class CollectionObject<T> extends Mappable<T> implements TemplateObject {
        private final Collection<T> collection;

        public CollectionObject(final Collection<T> collection, final BiConsumer<T, TemplateMap.Arguments> mapper) {
            super(mapper);
            this.collection = collection;
        }

        public Boolean isEmpty() {
            return collection == null || !collection.iterator().hasNext();
        }

        public Collection<T> getCollection() {
            return collection;
        }

        @Override
        public Stream<Supplier<String>> stream(final Locale locale) {
            throw new InvalidTemplateObjectEvaluationException("Collections should not be evaluated");
        }
    }

    public class ConditionObject implements TemplateObject {
        private final Boolean value;

        public ConditionObject(final Boolean value) {
            this.value = value;
        }

        public Boolean isTrue() {
            return value;
        }

        @Override
        public Stream<Supplier<String>> stream(final Locale locale) {
            throw new InvalidTemplateObjectEvaluationException("Booleans should not be evaluated");
        }
    }

    class StringObject implements TemplateObject {
        private final String value;

        public StringObject(final String value) {
            this.value = value;
        }

        @Override
        public Stream<Supplier<String>> stream(final Locale locale) {
            return Stream.of(() -> value);
        }
    }

    class SubTemplateObject implements TemplateObject {
        final Template subTemplate;

        public SubTemplateObject(final Template subTemplate) {
            this.subTemplate = subTemplate;
        }

        @Override
        public Stream<Supplier<String>> stream(final Locale locale) {
            return subTemplate.stream(locale);
        }

        public static class WithoutMaster extends SubTemplateObject {
            public WithoutMaster(Template subTemplate) {
                super(subTemplate);
            }

            @Override
            public Stream<Supplier<String>> stream(final Locale locale) {
                return subTemplate.streamWithoutMaster(locale);
            }
        }
    }

    //

    static abstract class Mappable<T> {

        private final BiConsumer<T, TemplateMap.Arguments> mapper;

        Mappable(final BiConsumer<T, TemplateMap.Arguments> mapper) {
            this.mapper = mapper;
        }

        public TemplateMap.Arguments map(final T object) {
            TemplateMap.Arguments arguments = new TemplateMap.Arguments();
            mapper.accept(object, arguments);
            return arguments;
        }

        public BiConsumer<T, TemplateMap.Arguments> getMapper() {
            return mapper;
        }
    }
}
