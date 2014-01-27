package sagex.phoenix.metadata;

import sagex.phoenix.tools.annotation.API;

@API(group="cast", proxy=true, prefix="Cast", resolver="phoenix.media.GetCastMember")
public interface ICastMember extends ISageCastMember {
}
