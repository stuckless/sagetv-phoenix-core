package sagex.phoenix.metadata;

import sagex.phoenix.metadata.proxy.SageProperty;

/**
 * Interface to represent Sage Metadata. This interface is just a placeholder.
 * If you create custom sage metadata fields, then they should implement this
 * class, sot that they can be handled by the metadata proxy classes.
 * <p/>
 * To create a metadata instance use {@link MetadataUtil}
 * .create(MetadataInterface)
 * <p/>
 * <pre>
 * {@link A_METADATA_INSTANCE} md = {@link MetadataUtil}.create(SAGE_METADATA_INTERFACE);
 * md.setTitle("MyTitle");
 * </pre>
 * <p/>
 * As a convenience, their is a complete metadata class called,
 * {@link ISageMetadataALL} that contains all the known metadata fields. You can
 * can add to this list by extending that interface, and then instanciating your
 * interface using the {@link MetadataUtil}.create()
 *
 * @author seans
 * @see ISageMetadataALL
 * @see ISageCustomMetadataRW
 * @see ISageFormatPropertyRO
 * @see ISagePropertyRO
 * @see ISagePropertyRW
 * @see ISageRolePropertyRW
 */
public interface ISageMetadata {
    public boolean isSet(SageProperty key);

    public void clear(SageProperty key);

    public String get(SageProperty key);

    public void set(SageProperty key, String value);
}
