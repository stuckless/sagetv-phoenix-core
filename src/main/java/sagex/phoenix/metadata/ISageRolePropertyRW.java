package sagex.phoenix.metadata;

import java.util.List;

import sagex.phoenix.metadata.proxy.SageProperty;

public interface ISageRolePropertyRW extends ISageMetadata {
	@SageProperty(value = "Actor", listFactory = "sagex.phoenix.metadata.proxy.CastMemberPropertyListFactory")
	public List<ICastMember> getActors();

	@SageProperty(value = "Lead Actor", listFactory = "sagex.phoenix.metadata.proxy.CastMemberPropertyListFactory")
	public List<ICastMember> getLeadActors();

	@SageProperty(value = "Supporting Actor", listFactory = "sagex.phoenix.metadata.proxy.CastMemberPropertyListFactory")
	public List<ICastMember> getSupportingActors();

	@SageProperty(value = "Actress", listFactory = "sagex.phoenix.metadata.proxy.CastMemberPropertyListFactory")
	public List<ICastMember> getActresses();

	@SageProperty(value = "Lead Actress", listFactory = "sagex.phoenix.metadata.proxy.CastMemberPropertyListFactory")
	public List<ICastMember> getLeadActresses();

	@SageProperty(value = "Supporting Actress", listFactory = "sagex.phoenix.metadata.proxy.CastMemberPropertyListFactory")
	public List<ICastMember> getSupportingActresses();

	@SageProperty(value = "Guest", listFactory = "sagex.phoenix.metadata.proxy.CastMemberPropertyListFactory")
	public List<ICastMember> getGuests();

	@SageProperty(value = "Guest Star", listFactory = "sagex.phoenix.metadata.proxy.CastMemberPropertyListFactory")
	public List<ICastMember> getGuestStars();

	@SageProperty(value = "Director", listFactory = "sagex.phoenix.metadata.proxy.CastMemberPropertyListFactory")
	public List<ICastMember> getDirectors();

	@SageProperty(value = "Producer", listFactory = "sagex.phoenix.metadata.proxy.CastMemberPropertyListFactory")
	public List<ICastMember> getProducers();

	@SageProperty(value = "Writer", listFactory = "sagex.phoenix.metadata.proxy.CastMemberPropertyListFactory")
	public List<ICastMember> getWriters();

	@SageProperty(value = "Choreographer", listFactory = "sagex.phoenix.metadata.proxy.CastMemberPropertyListFactory")
	public List<ICastMember> getChoreographers();

	@SageProperty(value = "Sports Figure", listFactory = "sagex.phoenix.metadata.proxy.CastMemberPropertyListFactory")
	public List<ICastMember> getSportsFigures();

	@SageProperty(value = "Coach", listFactory = "sagex.phoenix.metadata.proxy.CastMemberPropertyListFactory")
	public List<ICastMember> getCoaches();

	@SageProperty(value = "Host", listFactory = "sagex.phoenix.metadata.proxy.CastMemberPropertyListFactory")
	public List<ICastMember> getHosts();

	@SageProperty(value = "Executive Producer", listFactory = "sagex.phoenix.metadata.proxy.CastMemberPropertyListFactory")
	public List<ICastMember> getExecutiveProducers();

	@SageProperty(value = "Artist", listFactory = "sagex.phoenix.metadata.proxy.CastMemberPropertyListFactory")
	public List<ICastMember> getArtists();

	@SageProperty(value = "Album Artist", listFactory = "sagex.phoenix.metadata.proxy.CastMemberPropertyListFactory")
	public List<ICastMember> getAlbumArtists();

	@SageProperty(value = "Composer", listFactory = "sagex.phoenix.metadata.proxy.CastMemberPropertyListFactory")
	public List<ICastMember> getComposers();

	@SageProperty(value = "Judge", listFactory = "sagex.phoenix.metadata.proxy.CastMemberPropertyListFactory")
	public List<ICastMember> getJudges();

	@SageProperty(value = "Narrator", listFactory = "sagex.phoenix.metadata.proxy.CastMemberPropertyListFactory")
	public List<ICastMember> getNarrators();

	@SageProperty(value = "Contestant", listFactory = "sagex.phoenix.metadata.proxy.CastMemberPropertyListFactory")
	public List<ICastMember> getContestants();

	@SageProperty(value = "Correspondent", listFactory = "sagex.phoenix.metadata.proxy.CastMemberPropertyListFactory")
	public List<ICastMember> getCorrespondents();
}
