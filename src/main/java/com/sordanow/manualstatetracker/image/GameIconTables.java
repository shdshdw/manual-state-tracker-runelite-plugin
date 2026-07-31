/*
 * Copyright (c) 2026, Sordanow
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.sordanow.manualstatetracker.image;

import static com.sordanow.manualstatetracker.image.GameIconType.PRAYER;
import static com.sordanow.manualstatetracker.image.GameIconType.SKILL;
import static com.sordanow.manualstatetracker.image.GameIconType.SPELL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.runelite.api.gameval.SpriteID;

/**
 * The fixed icons the plugin can offer without the game being loaded: every prayer, every skill, and
 * every spell across the standard, ancient, lunar and Arceuus spellbooks.
 *
 * <p>Each entry names a {@code SpriteID} constant rather than a raw number, so a sprite that
 * disappears from the API is a compile error instead of a silently blank icon. Item icons are not
 * here; there are tens of thousands of them and {@link GameIconCatalogue} indexes those from the
 * game cache at runtime.</p>
 */
final class GameIconTables
{
	private static final List<GameIcon> ICONS = build();

	private GameIconTables()
	{
	}

	/** Every prayer and spell icon, in spellbook order. */
	static List<GameIcon> all()
	{
		return ICONS;
	}

	private static List<GameIcon> build()
	{
		final List<GameIcon> icons = new ArrayList<>();

		// PRAYERS
		icons.add(new GameIcon(PRAYER, SpriteID.Prayeron.THICK_SKIN, "Thick Skin"));
		icons.add(new GameIcon(PRAYER, SpriteID.Prayeron.BURST_OF_STRENGTH, "Burst of Strength"));
		icons.add(new GameIcon(PRAYER, SpriteID.Prayeron.CLARITY_OF_THOUGHT, "Clarity of Thought"));
		icons.add(new GameIcon(PRAYER, SpriteID.Prayeron.ROCK_SKIN, "Rock Skin"));
		icons.add(new GameIcon(PRAYER, SpriteID.Prayeron.SUPERHUMAN_STRENGTH, "Superhuman Strength"));
		icons.add(new GameIcon(PRAYER, SpriteID.Prayeron.IMPROVED_REFLEXES, "Improved Reflexes"));
		icons.add(new GameIcon(PRAYER, SpriteID.Prayeron.RAPID_RESTORE, "Rapid Restore"));
		icons.add(new GameIcon(PRAYER, SpriteID.Prayeron.RAPID_HEAL, "Rapid Heal"));
		icons.add(new GameIcon(PRAYER, SpriteID.Prayeron.PROTECT_ITEM, "Protect Item"));
		icons.add(new GameIcon(PRAYER, SpriteID.Prayeron.STEEL_SKIN, "Steel Skin"));
		icons.add(new GameIcon(PRAYER, SpriteID.Prayeron.ULTIMATE_STRENGTH, "Ultimate Strength"));
		icons.add(new GameIcon(PRAYER, SpriteID.Prayeron.INCREDIBLE_REFLEXES, "Incredible Reflexes"));
		icons.add(new GameIcon(PRAYER, SpriteID.Prayeron.PROTECT_FROM_MAGIC, "Protect from Magic"));
		icons.add(new GameIcon(PRAYER, SpriteID.Prayeron.PROTECT_FROM_MISSILES, "Protect from Missiles"));
		icons.add(new GameIcon(PRAYER, SpriteID.Prayeron.PROTECT_FROM_MELEE, "Protect from Melee"));
		icons.add(new GameIcon(PRAYER, SpriteID.Prayeron.REDEMPTION, "Redemption"));
		icons.add(new GameIcon(PRAYER, SpriteID.Prayeron.RETRIBUTION, "Retribution"));
		icons.add(new GameIcon(PRAYER, SpriteID.Prayeron.SMITE, "Smite"));
		icons.add(new GameIcon(PRAYER, SpriteID.Prayeron.SHARP_EYE, "Sharp Eye"));
		icons.add(new GameIcon(PRAYER, SpriteID.Prayeron.MYSTIC_WILL, "Mystic Will"));
		icons.add(new GameIcon(PRAYER, SpriteID.Prayeron.HAWK_EYE, "Hawk Eye"));
		icons.add(new GameIcon(PRAYER, SpriteID.Prayeron.MYSTIC_LORE, "Mystic Lore"));
		icons.add(new GameIcon(PRAYER, SpriteID.Prayeron.EAGLE_EYE, "Eagle Eye"));
		icons.add(new GameIcon(PRAYER, SpriteID.Prayeron.MYSTIC_MIGHT, "Mystic Might"));
		icons.add(new GameIcon(PRAYER, SpriteID.Prayeron.PROTECT_FROM_SUMMONING, "Protect from Summoning"));
		icons.add(new GameIcon(PRAYER, SpriteID.Prayeron.CHIVALRY, "Chivalry"));
		icons.add(new GameIcon(PRAYER, SpriteID.Prayeron.PIETY, "Piety"));
		icons.add(new GameIcon(PRAYER, SpriteID.Prayeron.PRESERVE, "Preserve"));
		icons.add(new GameIcon(PRAYER, SpriteID.Prayeron.RIGOUR, "Rigour"));
		icons.add(new GameIcon(PRAYER, SpriteID.Prayeron.AUGURY, "Augury"));
		icons.add(new GameIcon(PRAYER, SpriteID.Prayeron.DEADEYE, "Deadeye"));
		icons.add(new GameIcon(PRAYER, SpriteID.Prayeron.MYSTIC_VIGOUR, "Mystic Vigour"));

		// SKILLS
		icons.add(new GameIcon(SKILL, SpriteID.Staticons.ATTACK, "Attack"));
		icons.add(new GameIcon(SKILL, SpriteID.Staticons.STRENGTH, "Strength"));
		icons.add(new GameIcon(SKILL, SpriteID.Staticons.DEFENCE, "Defence"));
		icons.add(new GameIcon(SKILL, SpriteID.Staticons.RANGED, "Ranged"));
		icons.add(new GameIcon(SKILL, SpriteID.Staticons.PRAYER, "Prayer"));
		icons.add(new GameIcon(SKILL, SpriteID.Staticons.MAGIC, "Magic"));
		icons.add(new GameIcon(SKILL, SpriteID.Staticons.HITPOINTS, "Hitpoints"));
		icons.add(new GameIcon(SKILL, SpriteID.Staticons.AGILITY, "Agility"));
		icons.add(new GameIcon(SKILL, SpriteID.Staticons.HERBLORE, "Herblore"));
		icons.add(new GameIcon(SKILL, SpriteID.Staticons.THIEVING, "Thieving"));
		icons.add(new GameIcon(SKILL, SpriteID.Staticons.CRAFTING, "Crafting"));
		icons.add(new GameIcon(SKILL, SpriteID.Staticons.FLETCHING, "Fletching"));
		icons.add(new GameIcon(SKILL, SpriteID.Staticons.MINING, "Mining"));
		icons.add(new GameIcon(SKILL, SpriteID.Staticons.SMITHING, "Smithing"));
		icons.add(new GameIcon(SKILL, SpriteID.Staticons.FISHING, "Fishing"));
		icons.add(new GameIcon(SKILL, SpriteID.Staticons.COOKING, "Cooking"));
		icons.add(new GameIcon(SKILL, SpriteID.Staticons.FIREMAKING, "Firemaking"));
		icons.add(new GameIcon(SKILL, SpriteID.Staticons.WOODCUTTING, "Woodcutting"));
		icons.add(new GameIcon(SKILL, SpriteID.Staticons2.RUNECRAFT, "Runecraft"));
		icons.add(new GameIcon(SKILL, SpriteID.Staticons2.SLAYER, "Slayer"));
		icons.add(new GameIcon(SKILL, SpriteID.Staticons2.FARMING, "Farming"));
		icons.add(new GameIcon(SKILL, SpriteID.Staticons2.HUNTER, "Hunter"));
		icons.add(new GameIcon(SKILL, SpriteID.Staticons2.CONSTRUCTION, "Construction"));
		icons.add(new GameIcon(SKILL, SpriteID.Staticons2.TOTAL, "Total"));
		icons.add(new GameIcon(SKILL, SpriteID.Staticons2.SAILING, "Sailing"));

		// SPELLS: standard
		icons.add(new GameIcon(SPELL, SpriteID.Magicon.WIND_STRIKE, "Wind Strike"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon.CONFUSE, "Confuse"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon.WATER_STRIKE, "Water Strike"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon.LVL_1_ENCHANT, "Lvl 1 Enchant"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon.EARTH_STRIKE, "Earth Strike"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon.WEAKEN, "Weaken"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon.FIRE_STRIKE, "Fire Strike"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon.BONES_TO_BANANAS, "Bones to Bananas"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon.WIND_BOLT, "Wind Bolt"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon.CURSE, "Curse"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon.LOW_LEVEL_ALCHEMY, "Low Level Alchemy"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon.WATER_BOLT, "Water Bolt"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon.VARROCK_TELEPORT, "Varrock Teleport"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon.LVL_2_ENCHANT, "Lvl 2 Enchant"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon.EARTH_BOLT, "Earth Bolt"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon.LUMBRIDGE_TELEPORT, "Lumbridge Teleport"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon.TELEKINETIC_GRAB, "Telekinetic Grab"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon.FIRE_BOLT, "Fire Bolt"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon.FALADOR_TELEPORT, "Falador Teleport"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon.CRUMBLE_UNDEAD, "Crumble Undead"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon.WIND_BLAST, "Wind Blast"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon.SUPERHEAT_ITEM, "Superheat Item"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon.CAMELOT_TELEPORT, "Camelot Teleport"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon.WATER_BLAST, "Water Blast"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon.LVL_3_ENCHANT, "Lvl 3 Enchant"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon.EARTH_BLAST, "Earth Blast"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon.HIGH_LEVEL_ALCHEMY, "High Level Alchemy"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon.CHARGE_WATER_ORB, "Charge Water Orb"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon.LVL_4_ENCHANT, "Lvl 4 Enchant"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon.FIRE_BLAST, "Fire Blast"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon.CHARGE_EARTH_ORB, "Charge Earth Orb"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon.WIND_WAVE, "Wind Wave"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon.CHARGE_FIRE_ORB, "Charge Fire Orb"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon.WATER_WAVE, "Water Wave"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon.CHARGE_AIR_ORB, "Charge Air Orb"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon.LVL_5_ENCHANT, "Lvl 5 Enchant"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon.EARTH_WAVE, "Earth Wave"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon.FIRE_WAVE, "Fire Wave"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon.IBAN_BLAST, "Iban Blast"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon.ARDOUGNE_TELEPORT, "Ardougne Teleport"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon.WATCHTOWER_TELEPORT, "Watchtower Teleport"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon.VULNERABILITY, "Vulnerability"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon.ENFEEBLE, "Enfeeble"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon.STUN, "Stun"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon.FLAMES_OF_ZAMORAK, "Flames of Zamorak"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon.CLAWS_OF_GUTHIX, "Claws of Guthix"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon.SARADOMIN_STRIKE, "Saradomin Strike"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon.CALL_ANIMAL, "Call Animal"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon.RAISE_SKELETON, "Raise Skeleton"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon.SUMMON_DEMON, "Summon Demon"));

		// SPELLS: standard continued and ancient
		icons.add(new GameIcon(SPELL, SpriteID.Magicon2.BIND, "Bind"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon2.SNARE, "Snare"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon2.ENTANGLE, "Entangle"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon2.CHARGE, "Charge"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon2.TROLLHEIM_TELEPORT, "Trollheim Teleport"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon2.MAGIC_DART, "Magic Dart"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon2.ICE_RUSH, "Ice Rush"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon2.ICE_BURST, "Ice Burst"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon2.ICE_BLITZ, "Ice Blitz"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon2.ICE_BARRAGE, "Ice Barrage"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon2.SMOKE_RUSH, "Smoke Rush"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon2.SMOKE_BURST, "Smoke Burst"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon2.SMOKE_BLITZ, "Smoke Blitz"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon2.SMOKE_BARRAGE, "Smoke Barrage"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon2.BLOOD_RUSH, "Blood Rush"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon2.BLOOD_BURST, "Blood Burst"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon2.BLOOD_BLITZ, "Blood Blitz"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon2.BLOOD_BARRAGE, "Blood Barrage"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon2.SHADOW_RUSH, "Shadow Rush"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon2.SHADOW_BURST, "Shadow Burst"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon2.SHADOW_BLITZ, "Shadow Blitz"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon2.SHADOW_BARRAGE, "Shadow Barrage"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon2.PADDEWWA_TELEPORT, "Paddewwa Teleport"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon2.SENNTISTEN_TELEPORT, "Senntisten Teleport"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon2.KHARYRLL_TELEPORT, "Kharyrll Teleport"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon2.LASSAR_TELEPORT, "Lassar Teleport"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon2.DAREEYAK_TELEPORT, "Dareeyak Teleport"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon2.CARRALLANGAR_TELEPORT, "Carrallangar Teleport"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon2.ANNAKARL_TELEPORT, "Annakarl Teleport"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon2.GHORROCK_TELEPORT, "Ghorrock Teleport"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon2.TELEOTHER_LUMBRIDGE, "Teleother Lumbridge"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon2.TELEOTHER_FALADOR, "Teleother Falador"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon2.TELEOTHER_CAMELOT, "Teleother Camelot"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon2.TELE_BLOCK, "Tele Block"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon2.LVL_6_ENCHANT, "Lvl 6 Enchant"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon2.BONES_TO_PEACHES, "Bones to Peaches"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon2.TELEPORT_TO_HOUSE, "Teleport to House"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon2.LUMBRIDGE_HOME_TELEPORT, "Lumbridge Home Teleport"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon2.TELEPORT_TO_APE_ATOLL, "Teleport to Ape Atoll"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon2.ENCHANT_CROSSBOW_BOLT, "Enchant Crossbow Bolt"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon2.TELEPORT_TO_BOUNTY_TARGET, "Teleport to Bounty Target"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon2.TELEPORT_TO_KOUREND, "Teleport to Kourend"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon2.LVL_7_ENCHANT, "Lvl 7 Enchant"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon2.WIND_SURGE, "Wind Surge"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon2.WATER_SURGE, "Water Surge"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon2.EARTH_SURGE, "Earth Surge"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon2.FIRE_SURGE, "Fire Surge"));
		icons.add(new GameIcon(SPELL, SpriteID.Magicon2.CIVITAS_ILLA_FORTIS_TELEPORT, "Civitas Illa Fortis Teleport"));

		// SPELLS: lunar
		icons.add(new GameIcon(SPELL, SpriteID.LunarMagicOn.BAKE_PIE, "Bake Pie"));
		icons.add(new GameIcon(SPELL, SpriteID.LunarMagicOn.MOONCLAN_TELEPORT, "Moonclan Teleport"));
		icons.add(new GameIcon(SPELL, SpriteID.LunarMagicOn.WATERBIRTH_TELEPORT, "Waterbirth Teleport"));
		icons.add(new GameIcon(SPELL, SpriteID.LunarMagicOn.BOW_AND_ARROW, "Bow and Arrow"));
		icons.add(new GameIcon(SPELL, SpriteID.LunarMagicOn.BARBARIAN_TELEPORT, "Barbarian Teleport"));
		icons.add(new GameIcon(SPELL, SpriteID.LunarMagicOn.SUPERGLASS_MAKE, "Superglass Make"));
		icons.add(new GameIcon(SPELL, SpriteID.LunarMagicOn.KHAZARD_TELEPORT, "Khazard Teleport"));
		icons.add(new GameIcon(SPELL, SpriteID.LunarMagicOn.STRING_JEWELLERY, "String Jewellery"));
		icons.add(new GameIcon(SPELL, SpriteID.LunarMagicOn.BOOST_POTION_SHARE, "Boost Potion Share"));
		icons.add(new GameIcon(SPELL, SpriteID.LunarMagicOn.MAGIC_IMBUE, "Magic Imbue"));
		icons.add(new GameIcon(SPELL, SpriteID.LunarMagicOn.FERTILE_SOIL, "Fertile Soil"));
		icons.add(new GameIcon(SPELL, SpriteID.LunarMagicOn.STAT_RESTORE_POT_SHARE, "Stat Restore Pot Share"));
		icons.add(new GameIcon(SPELL, SpriteID.LunarMagicOn.FISHING_GUILD_TELEPORT, "Fishing Guild Teleport"));
		icons.add(new GameIcon(SPELL, SpriteID.LunarMagicOn.CATHERBY_TELEPORT, "Catherby Teleport"));
		icons.add(new GameIcon(SPELL, SpriteID.LunarMagicOn.ICE_PLATEAU_TELEPORT, "Ice Plateau Teleport"));
		icons.add(new GameIcon(SPELL, SpriteID.LunarMagicOn.ENERGY_TRANSFER, "Energy Transfer"));
		icons.add(new GameIcon(SPELL, SpriteID.LunarMagicOn.CURE_OTHER, "Cure Other"));
		icons.add(new GameIcon(SPELL, SpriteID.LunarMagicOn.HEAL_OTHER, "Heal Other"));
		icons.add(new GameIcon(SPELL, SpriteID.LunarMagicOn.VENGEANCE_OTHER, "Vengeance Other"));
		icons.add(new GameIcon(SPELL, SpriteID.LunarMagicOn.CURE_ME, "Cure Me"));
		icons.add(new GameIcon(SPELL, SpriteID.LunarMagicOn.GEOMANCY, "Geomancy"));
		icons.add(new GameIcon(SPELL, SpriteID.LunarMagicOn.VENGEANCE, "Vengeance"));
		icons.add(new GameIcon(SPELL, SpriteID.LunarMagicOn.CURE_GROUP, "Cure Group"));
		icons.add(new GameIcon(SPELL, SpriteID.LunarMagicOn.HEAL_GROUP, "Heal Group"));
		icons.add(new GameIcon(SPELL, SpriteID.LunarMagicOn.CURE_PLANT, "Cure Plant"));
		icons.add(new GameIcon(SPELL, SpriteID.LunarMagicOn.NPC_CONTACT, "NPC Contact"));
		icons.add(new GameIcon(SPELL, SpriteID.LunarMagicOn.TELE_GROUP_MOONCLAN, "Tele Group Moonclan"));
		icons.add(new GameIcon(SPELL, SpriteID.LunarMagicOn.TELE_GROUP_WATERBIRTH, "Tele Group Waterbirth"));
		icons.add(new GameIcon(SPELL, SpriteID.LunarMagicOn.TELE_GROUP_BARBARIAN, "Tele Group Barbarian"));
		icons.add(new GameIcon(SPELL, SpriteID.LunarMagicOn.TELE_GROUP_KHAZARD, "Tele Group Khazard"));
		icons.add(new GameIcon(SPELL, SpriteID.LunarMagicOn.TELE_GROUP_FISHING_GUILD, "Tele Group Fishing Guild"));
		icons.add(new GameIcon(SPELL, SpriteID.LunarMagicOn.TELE_GROUP_CATHERBY, "Tele Group Catherby"));
		icons.add(new GameIcon(SPELL, SpriteID.LunarMagicOn.TELE_GROUP_ICE_PLATEAU, "Tele Group Ice Plateau"));
		icons.add(new GameIcon(SPELL, SpriteID.LunarMagicOn.STAT_SPY, "Stat Spy"));
		icons.add(new GameIcon(SPELL, SpriteID.LunarMagicOn.MONSTER_EXAMINE, "Monster Examine"));
		icons.add(new GameIcon(SPELL, SpriteID.LunarMagicOn.HUMIDIFY, "Humidify"));
		icons.add(new GameIcon(SPELL, SpriteID.LunarMagicOn.HUNTER_KIT, "Hunter Kit"));
		icons.add(new GameIcon(SPELL, SpriteID.LunarMagicOn.DREAM, "Dream"));
		icons.add(new GameIcon(SPELL, SpriteID.LunarMagicOn.PLANK_MAKE, "Plank Make"));
		icons.add(new GameIcon(SPELL, SpriteID.LunarMagicOn.SPELLBOOK_SWAP, "Spellbook Swap"));
		icons.add(new GameIcon(SPELL, SpriteID.LunarMagicOn.TAN_LEATHER, "Tan Leather"));
		icons.add(new GameIcon(SPELL, SpriteID.LunarMagicOn.RECHARGE_DRAGONSTONE, "Recharge Dragonstone"));
		icons.add(new GameIcon(SPELL, SpriteID.LunarMagicOn.SPIN_FLAX, "Spin Flax"));
		icons.add(new GameIcon(SPELL, SpriteID.LunarMagicOn.OURANIA_TELEPORT, "Ourania Teleport"));

		// SPELLS: arceuus
		icons.add(new GameIcon(SPELL, SpriteID.MagicNecroOn.BASIC_REANIMATION, "Basic Reanimation"));
		icons.add(new GameIcon(SPELL, SpriteID.MagicNecroOn.ADEPT_REANIMATION, "Adept Reanimation"));
		icons.add(new GameIcon(SPELL, SpriteID.MagicNecroOn.EXPERT_REANIMATION, "Expert Reanimation"));
		icons.add(new GameIcon(SPELL, SpriteID.MagicNecroOn.MASTER_REANIMATION, "Master Reanimation"));
		icons.add(new GameIcon(SPELL, SpriteID.MagicNecroOn.ARCEUUS_LIBRARY_TELEPORT, "Arceuus Library Teleport"));
		icons.add(new GameIcon(SPELL, SpriteID.MagicNecroOn.DRAYNOR_MANOR_TELEPORT, "Draynor Manor Teleport"));
		icons.add(new GameIcon(SPELL, SpriteID.MagicNecroOn.SALVE_GRAVEYARD_TELEPORT, "Salve Graveyard Teleport"));
		icons.add(new GameIcon(SPELL, SpriteID.MagicNecroOn.MIND_ALTAR_TELEPORT, "Mind Altar Teleport"));
		icons.add(new GameIcon(SPELL, SpriteID.MagicNecroOn.RESPAWN_TELEPORT, "Respawn Teleport"));
		icons.add(new GameIcon(SPELL, SpriteID.MagicNecroOn.FENKENSTRAINS_CASTLE_TELEPORT, "Fenkenstrains Castle Teleport"));
		icons.add(new GameIcon(SPELL, SpriteID.MagicNecroOn.WEST_ARDOUGNE_TELEPORT, "West Ardougne Teleport"));
		icons.add(new GameIcon(SPELL, SpriteID.MagicNecroOn.HARMONY_ISLAND_TELEPORT, "Harmony Island Teleport"));
		icons.add(new GameIcon(SPELL, SpriteID.MagicNecroOn.BARROWS_TELEPORT, "Barrows Teleport"));
		icons.add(new GameIcon(SPELL, SpriteID.MagicNecroOn.APE_ATOLL_TELEPORT, "Ape Atoll Teleport"));
		icons.add(new GameIcon(SPELL, SpriteID.MagicNecroOn.CEMETERY_TELEPORT, "Cemetery Teleport"));
		icons.add(new GameIcon(SPELL, SpriteID.MagicNecroOn.RESURRECT_CROPS, "Resurrect Crops"));
		icons.add(new GameIcon(SPELL, SpriteID.MagicNecroOn.GHOSTLY_GRASP, "Ghostly Grasp"));
		icons.add(new GameIcon(SPELL, SpriteID.MagicNecroOn.SKELETAL_GRASP, "Skeletal Grasp"));
		icons.add(new GameIcon(SPELL, SpriteID.MagicNecroOn.UNDEAD_GRASP, "Undead Grasp"));
		icons.add(new GameIcon(SPELL, SpriteID.MagicNecroOn.RESURRECT_LESSER_GHOST, "Resurrect Lesser Ghost"));
		icons.add(new GameIcon(SPELL, SpriteID.MagicNecroOn.INFERIOR_DEMONBANE, "Inferior Demonbane"));
		icons.add(new GameIcon(SPELL, SpriteID.MagicNecroOn.SUPERIOR_DEMONBANE, "Superior Demonbane"));
		icons.add(new GameIcon(SPELL, SpriteID.MagicNecroOn.DARK_DEMONBANE, "Dark Demonbane"));
		icons.add(new GameIcon(SPELL, SpriteID.MagicNecroOn.MARK_OF_DARKNESS, "Mark of Darkness"));
		icons.add(new GameIcon(SPELL, SpriteID.MagicNecroOn.WARD_OF_ARCEUUS, "Ward of Arceuus"));
		icons.add(new GameIcon(SPELL, SpriteID.MagicNecroOn.LESSER_CORRUPTION, "Lesser Corruption"));
		icons.add(new GameIcon(SPELL, SpriteID.MagicNecroOn.GREATER_CORRUPTION, "Greater Corruption"));
		icons.add(new GameIcon(SPELL, SpriteID.MagicNecroOn.DEATH_CHARGE, "Death Charge"));
		icons.add(new GameIcon(SPELL, SpriteID.MagicNecroOn.DEMONIC_OFFERING, "Demonic Offering"));
		icons.add(new GameIcon(SPELL, SpriteID.MagicNecroOn.SINISTER_OFFERING, "Sinister Offering"));
		icons.add(new GameIcon(SPELL, SpriteID.MagicNecroOn.SHADOW_VEIL, "Shadow Veil"));
		icons.add(new GameIcon(SPELL, SpriteID.MagicNecroOn.DARK_LURE, "Dark Lure"));
		icons.add(new GameIcon(SPELL, SpriteID.MagicNecroOn.VILE_VIGOUR, "Vile Vigour"));
		icons.add(new GameIcon(SPELL, SpriteID.MagicNecroOn.DEGRIME, "Degrime"));
		icons.add(new GameIcon(SPELL, SpriteID.MagicNecroOn.RESURRECT_SUPERIOR_SKELETON, "Resurrect Superior Skeleton"));
		icons.add(new GameIcon(SPELL, SpriteID.MagicNecroOn.RESURRECT_GREATER_ZOMBIE, "Resurrect Greater Zombie"));

		return Collections.unmodifiableList(icons);
	}
}
