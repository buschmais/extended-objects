package com.buschmais.xo.impl.metadata;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.buschmais.xo.api.XOException;
import com.buschmais.xo.api.bootstrap.XOUnit;
import com.buschmais.xo.api.metadata.type.CompositeTypeMetadata;
import com.buschmais.xo.api.metadata.type.DatastoreEntityMetadata;
import com.buschmais.xo.api.metadata.type.EntityTypeMetadata;
import com.buschmais.xo.api.metadata.type.TypeMetadata;

import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allows resolving types from entity discriminators as provided by the
 * datastores.
 *
 * @param <Discriminator>
 *     The discriminator type of the datastore (e.g. Neo4j labels or
 *     strings for JSON stores).
 */
public class EntityTypeMetadataResolver<EntityMetadata extends DatastoreEntityMetadata<Discriminator>, Discriminator> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityTypeMetadataResolver.class);

    private final Map<EntityTypeMetadata<EntityMetadata>, Set<Discriminator>> aggregatedDiscriminators = new HashMap<>();
    private final Map<Discriminator, Set<EntityTypeMetadata<EntityMetadata>>> typeMetadataByDiscriminator = new HashMap<>();
    private final Map<EntityTypeMetadata<EntityMetadata>, Set<EntityTypeMetadata<EntityMetadata>>> aggregatedSuperTypes = new HashMap<>();
    private final Map<EntityTypeMetadata<EntityMetadata>, Set<EntityTypeMetadata<EntityMetadata>>> aggregatedSubTypes = new HashMap<>();

    private final Map<Set<Discriminator>, CompositeTypeMetadata<EntityTypeMetadata<EntityMetadata>>> cache = new ConcurrentHashMap<>();

    /**
     * Constructor.
     *
     * @param metadataByType
     *     A map of all types with their metadata.
     */
    public EntityTypeMetadataResolver(Map<Class<?>, TypeMetadata> metadataByType, XOUnit.MappingConfiguration mappingConfiguration) {
        LOGGER.debug("Type metadata = '{}'", metadataByType);
        // Aggregate all super types
        for (TypeMetadata typeMetadata : metadataByType.values()) {
            if (typeMetadata instanceof EntityTypeMetadata) {
                EntityTypeMetadata<EntityMetadata> entityTypeMetadata = (EntityTypeMetadata<EntityMetadata>) typeMetadata;
                aggregateSuperTypes(entityTypeMetadata);
            }
        }

        Map<Set<Discriminator>, Set<EntityTypeMetadata<EntityMetadata>>> entityMetadataByDiscriminators = new HashMap<>();
        for (TypeMetadata typeMetadata : metadataByType.values()) {
            if (typeMetadata instanceof EntityTypeMetadata) {
                EntityTypeMetadata<EntityMetadata> entityTypeMetadata = (EntityTypeMetadata<EntityMetadata>) typeMetadata;
                Set<Discriminator> discriminators = getAggregatedDiscriminators(entityTypeMetadata);
                Set<EntityTypeMetadata<EntityMetadata>> typeMetadataOfDiscriminators = entityMetadataByDiscriminators.get(discriminators);
                if (typeMetadataOfDiscriminators == null) {
                    typeMetadataOfDiscriminators = new HashSet<>();
                    entityMetadataByDiscriminators.put(discriminators, typeMetadataOfDiscriminators);
                }
                typeMetadataOfDiscriminators.add(entityTypeMetadata);
                LOGGER.debug("Aggregated discriminators of '{}' = '{}'", typeMetadata, discriminators);
            }
        }

        for (TypeMetadata typeMetadata : metadataByType.values()) {
            if (typeMetadata instanceof EntityTypeMetadata) {
                EntityTypeMetadata<EntityMetadata> metadata = (EntityTypeMetadata<EntityMetadata>) typeMetadata;
                Set<Discriminator> discriminators = getAggregatedDiscriminators(metadata);
                for (Discriminator discriminator : discriminators) {
                    Set<EntityTypeMetadata<EntityMetadata>> entityTypeMetadata = typeMetadataByDiscriminator.get(discriminator);
                    if (entityTypeMetadata == null) {
                        entityTypeMetadata = new HashSet<>();
                        typeMetadataByDiscriminator.put(discriminator, entityTypeMetadata);
                    }
                    entityTypeMetadata.add(metadata);
                }
            }
        }
        LOGGER.debug("Type metadata by discriminators: '{}'", typeMetadataByDiscriminator);
        List<String> messages = new ArrayList<>();
        for (Map.Entry<Set<Discriminator>, Set<EntityTypeMetadata<EntityMetadata>>> entry : entityMetadataByDiscriminators.entrySet()) {
            if (entry.getValue()
                .size() > 1) {
                String message = String.format("%s use the same set of discriminators %s.", entry.getValue(), entry.getKey());
                messages.add(message);

            }
        }
        if (!messages.isEmpty() && mappingConfiguration.isStrictValidation()) {
            throw new XOException("Mapping problems detected: " + messages);
        } else {
            for (String message : messages) {
                LOGGER.warn(message);
            }
        }
    }

    private Set<EntityTypeMetadata<EntityMetadata>> aggregateSuperTypes(EntityTypeMetadata<EntityMetadata> entityTypeMetadata) {
        Set<EntityTypeMetadata<EntityMetadata>> superTypes = aggregatedSuperTypes.get(entityTypeMetadata);
        if (superTypes == null) {
            superTypes = new HashSet<>();
            for (TypeMetadata metadata : entityTypeMetadata.getSuperTypes()) {
                if (metadata instanceof EntityTypeMetadata) {
                    EntityTypeMetadata<EntityMetadata> superTypeMetadata = (EntityTypeMetadata<EntityMetadata>) metadata;
                    superTypes.add(superTypeMetadata);
                    addSubType(superTypeMetadata, entityTypeMetadata);
                    superTypes.addAll(aggregateSuperTypes(superTypeMetadata));
                    for (EntityTypeMetadata<EntityMetadata> superType : superTypes) {
                        addSubType(superType, entityTypeMetadata);
                    }
                }
            }
            aggregatedSuperTypes.put(entityTypeMetadata, superTypes);
        }
        return superTypes;
    }

    private void addSubType(EntityTypeMetadata<EntityMetadata> superType, EntityTypeMetadata<EntityMetadata> subType) {
        Set<EntityTypeMetadata<EntityMetadata>> subTypes = aggregatedSubTypes.computeIfAbsent(superType, k -> new HashSet<>());
        subTypes.add(subType);
    }

    /**
     * Determine the set of discriminators for one type, i.e. the discriminator of
     * the type itself and of all it's super types.
     *
     * @param typeMetadata
     *     The type.
     * @return The set of discriminators.
     */
    private Set<Discriminator> getAggregatedDiscriminators(EntityTypeMetadata<EntityMetadata> typeMetadata) {
        return aggregatedDiscriminators.computeIfAbsent(typeMetadata, k -> {
            Set<Discriminator> discriminators = new HashSet<>();
            Discriminator discriminator = typeMetadata.getDatastoreMetadata()
                .getDiscriminator();
            if (discriminator != null) {
                discriminators.add(discriminator);
            }
            for (EntityTypeMetadata<EntityMetadata> superTypeMetadata : aggregatedSuperTypes.get(typeMetadata)) {
                discriminator = superTypeMetadata.getDatastoreMetadata()
                    .getDiscriminator();
                if (discriminator != null) {
                    discriminators.add(discriminator);
                }
            }
            return discriminators;
        });
    }

    /**
     * Return a {@link CompositeTypeMetadata} containing all types matching to the given
     * entity discriminators.
     *
     * @param discriminators
     *     The discriminators.
     * @return The {@link CompositeTypeMetadata}.
     */
    public CompositeTypeMetadata<EntityTypeMetadata<EntityMetadata>> getDynamicType(Set<Discriminator> discriminators) {
        return cache.computeIfAbsent(ImmutableSet.<Discriminator>builderWithExpectedSize(discriminators.size())
            .addAll(discriminators)
            .build(), key -> {
            LOGGER.debug("Cache miss for discriminators {}.", key);
            Set<EntityTypeMetadata<EntityMetadata>> metadata = new HashSet<>();
            for (Discriminator discriminator : key) {
                Set<EntityTypeMetadata<EntityMetadata>> candidates = typeMetadataByDiscriminator.get(discriminator);
                if (candidates != null) {
                    for (EntityTypeMetadata<EntityMetadata> candidate : candidates) {
                        Set<EntityTypeMetadata<EntityMetadata>> candidateSubTypes = aggregatedSubTypes.get(candidate);
                        if (candidateSubTypes == null || !containsAny(metadata, candidateSubTypes)) {
                            Set<Discriminator> entityDiscriminators = aggregatedDiscriminators.get(candidate);
                            if (key.size() >= entityDiscriminators.size() && key.containsAll(entityDiscriminators)) {
                                metadata.add(candidate);
                                // Remove all super types as they are already represented by the current type
                                metadata.removeAll(aggregatedSuperTypes.get(candidate));
                            }
                        }
                    }
                }
            }
            return new CompositeTypeMetadata<>(metadata);
        });
    }

    /**
     * Determines if any given element in another {@link Collection} is contained in
     * this set.
     *
     * @param set
     *     The set.
     * @param other
     *     The other {@link Collection}.
     * @return <code>true</code> if any other element is contained.
     */
    public <T> boolean containsAny(Set<T> set, Collection<T> other) {
        for (T t : other) {
            if (set.contains(t)) {
                return true;
            }
        }
        return false;
    }

    public Set<Discriminator> getDiscriminators(EntityTypeMetadata<EntityMetadata> entityTypeMetadata) {
        Set<Discriminator> discriminators = aggregatedDiscriminators.get(entityTypeMetadata);
        return discriminators != null ? discriminators : Collections.<Discriminator>emptySet();
    }
}
