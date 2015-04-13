package forge.ai;

import java.util.EnumMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import forge.ai.ability.*;
import forge.game.ability.ApiType;
import forge.util.ReflectionUtil;

public enum SpellApiToAi {
    Converter;

    private final Map<ApiType, SpellAbilityAi> apiToInstance = new EnumMap<>(ApiType.class);

    // Do the extra copy to make an actual EnumMap (faster)
    private final Map<ApiType, Class<? extends SpellAbilityAi>> apiToClass = Maps.newEnumMap(ImmutableMap
            .<ApiType, Class<? extends SpellAbilityAi>>builder()
            .put(ApiType.Abandon, AlwaysPlayAi.class)
            .put(ApiType.ActivateAbility, ActivateAbilityAi.class)
            .put(ApiType.AddOrRemoveCounter, CountersPutOrRemoveAi.class)
            .put(ApiType.AddPhase, AddPhaseAi.class)
            .put(ApiType.AddTurn, AddTurnAi.class)
            .put(ApiType.Animate, AnimateAi.class)
            .put(ApiType.AnimateAll, AnimateAllAi.class)
            .put(ApiType.Attach, AttachAi.class)
            .put(ApiType.Balance, BalanceAi.class)
            .put(ApiType.BecomesBlocked, BecomesBlockedAi.class)
            .put(ApiType.BidLife, BidLifeAi.class)
            .put(ApiType.Bond, BondAi.class)
            .put(ApiType.ChangeTargets, ChangeTargetsAi.class)
            .put(ApiType.ChangeZone, ChangeZoneAi.class)
            .put(ApiType.ChangeZoneAll, ChangeZoneAllAi.class)
            .put(ApiType.Charm, CharmAi.class)
            .put(ApiType.ChooseCard, ChooseCardAi.class)
            .put(ApiType.ChooseColor, ChooseColorAi.class)
            .put(ApiType.ChooseDirection, ChooseDirectionAi.class)
            .put(ApiType.ChooseNumber, ChooseNumberAi.class)
            .put(ApiType.ChoosePlayer, ChoosePlayerAi.class)
            .put(ApiType.ChooseSource, ChooseSourceAi.class)
            .put(ApiType.ChooseType, ChooseTypeAi.class)
            .put(ApiType.Clash, ClashAi.class)
            .put(ApiType.Cleanup, AlwaysPlayAi.class)
            .put(ApiType.Clone, CloneAi.class)
            .put(ApiType.CopyPermanent, CopyPermanentAi.class)
            .put(ApiType.CopySpellAbility, CanPlayAsDrawbackAi.class)
            .put(ApiType.ControlPlayer, CannotPlayAi.class)
            .put(ApiType.ControlSpell, CannotPlayAi.class)
            .put(ApiType.Counter, CounterAi.class)
            .put(ApiType.DamageAll, DamageAllAi.class)
            .put(ApiType.DealDamage, DamageDealAi.class)
            .put(ApiType.Debuff, DebuffAi.class)
            .put(ApiType.DeclareCombatants, CannotPlayAi.class)
            .put(ApiType.DelayedTrigger, DelayedTriggerAi.class)
            .put(ApiType.Destroy, DestroyAi.class)
            .put(ApiType.DestroyAll, DestroyAllAi.class)
            .put(ApiType.Dig, DigAi.class)
            .put(ApiType.DigUntil, DigUntilAi.class)
            .put(ApiType.Discard, DiscardAi.class)
            .put(ApiType.DrainMana, DrainManaAi.class)
            .put(ApiType.Draw, DrawAi.class)
            .put(ApiType.EachDamage, DamageEachAi.class)
            .put(ApiType.Effect, EffectAi.class)
            .put(ApiType.Encode, EncodeAi.class)
            .put(ApiType.EndTurn, EndTurnAi.class)
            .put(ApiType.ExchangeLife, LifeExchangeAi.class)
            .put(ApiType.ExchangeControl, ControlExchangeAi.class)
            .put(ApiType.ExchangeControlVariant, CannotPlayAi.class)
            .put(ApiType.ExchangePower, PowerExchangeAi.class)
            .put(ApiType.ExchangeZone, ZoneExchangeAi.class)
            .put(ApiType.Fight, FightAi.class)
            .put(ApiType.FlipACoin, FlipACoinAi.class)
            .put(ApiType.Fog, FogAi.class)
            .put(ApiType.GainControl, ControlGainAi.class)
            .put(ApiType.GainLife, LifeGainAi.class)
            .put(ApiType.GainOwnership, CannotPlayAi.class)
            .put(ApiType.GenericChoice, ChooseGenericEffectAi.class)
            .put(ApiType.LoseLife, LifeLoseAi.class)
            .put(ApiType.LosesGame, GameLossAi.class)
            .put(ApiType.Mana, ManaEffectAi.class)
            .put(ApiType.ManaReflected, CannotPlayAi.class)
            .put(ApiType.Manifest, ManifestAi.class)
            .put(ApiType.Mill, MillAi.class)
            .put(ApiType.MoveCounter, CountersMoveAi.class)
            .put(ApiType.MultiplePiles, CannotPlayAi.class)
            .put(ApiType.MustAttack, MustAttackAi.class)
            .put(ApiType.MustBlock, MustBlockAi.class)
            .put(ApiType.NameCard, ChooseCardNameAi.class)
            .put(ApiType.PeekAndReveal, PeekAndRevealAi.class)
            .put(ApiType.PermanentCreature, PermanentCreatureAi.class)
            .put(ApiType.PermanentNoncreature, PermanentNoncreatureAi.class)
            .put(ApiType.Phases, PhasesAi.class)
            .put(ApiType.Planeswalk, AlwaysPlayAi.class)
            .put(ApiType.Play, PlayAi.class)
            .put(ApiType.PlayLandVariant, CannotPlayAi.class)
            .put(ApiType.Poison, PoisonAi.class)
            .put(ApiType.PreventDamage, DamagePreventAi.class)
            .put(ApiType.PreventDamageAll, DamagePreventAllAi.class)
            .put(ApiType.Proliferate, CountersProliferateAi.class)
            .put(ApiType.Protection, ProtectAi.class)
            .put(ApiType.ProtectionAll, ProtectAllAi.class)
            .put(ApiType.Pump, PumpAi.class)
            .put(ApiType.PumpAll, PumpAllAi.class)
            .put(ApiType.PutCounter, CountersPutAi.class)
            .put(ApiType.PutCounterAll, CountersPutAllAi.class)
            .put(ApiType.RearrangeTopOfLibrary, RearrangeTopOfLibraryAi.class)
            .put(ApiType.Regenerate, RegenerateAi.class)
            .put(ApiType.RegenerateAll, RegenerateAllAi.class)
            .put(ApiType.RemoveCounter, CountersRemoveAi.class)
            .put(ApiType.RemoveCounterAll, CannotPlayAi.class)
            .put(ApiType.RemoveFromCombat, RemoveFromCombatAi.class)
            .put(ApiType.ReorderZone, AlwaysPlayAi.class)
            .put(ApiType.Repeat, RepeatAi.class)
            .put(ApiType.RepeatEach, RepeatEachAi.class)
            .put(ApiType.RestartGame, RestartGameAi.class)
            .put(ApiType.Reveal, RevealAi.class)
            .put(ApiType.RevealHand, RevealHandAi.class)
            .put(ApiType.ReverseTurnOrder, AlwaysPlayAi.class)
            .put(ApiType.RollPlanarDice, RollPlanarDiceAi.class)
            .put(ApiType.RunSVarAbility, AlwaysPlayAi.class)
            .put(ApiType.Sacrifice, SacrificeAi.class)
            .put(ApiType.SacrificeAll, SacrificeAllAi.class)
            .put(ApiType.Scry, ScryAi.class)
            .put(ApiType.SetInMotion, AlwaysPlayAi.class)
            .put(ApiType.SetLife, LifeSetAi.class)
            .put(ApiType.SetState, SetStateAi.class)
            .put(ApiType.Shuffle, ShuffleAi.class)
            .put(ApiType.SkipTurn, SkipTurnAi.class)
            .put(ApiType.StoreMap, StoreMapAi.class)
            .put(ApiType.StoreSVar, StoreSVarAi.class)
            .put(ApiType.Tap, TapAi.class)
            .put(ApiType.TapAll, TapAllAi.class)
            .put(ApiType.TapOrUntap, TapOrUntapAi.class)
            .put(ApiType.TapOrUntapAll, TapOrUntapAllAi.class)
            .put(ApiType.Token, TokenAi.class)
            .put(ApiType.TwoPiles, TwoPilesAi.class)
            .put(ApiType.Unattach, CannotPlayAi.class)
            .put(ApiType.UnattachAll, UnattachAllAi.class)
            .put(ApiType.Untap, UntapAi.class)
            .put(ApiType.UntapAll, UntapAllAi.class)
            .put(ApiType.Vote, VoteAi.class)
            .put(ApiType.WinsGame, GameWinAi.class)

            .put(ApiType.InternalEtbReplacement, CanPlayAsDrawbackAi.class)
            .put(ApiType.InternalLegendaryRule, LegendaryRuleAi.class)
            .put(ApiType.InternalHaunt, HauntAi.class)
            .put(ApiType.InternalIgnoreEffect, CannotPlayAi.class)
            .build());

    public SpellAbilityAi get(final ApiType api) {
        SpellAbilityAi result = apiToInstance.get(api);
        if (null == result) {
            Class<? extends SpellAbilityAi> clz = apiToClass.get(api);
            if (null == clz) {
                System.err.println("No AI assigned for API: " + api);
                clz = CannotPlayAi.class;
            }
            result = ReflectionUtil.makeDefaultInstanceOf(clz);
            apiToInstance.put(api, result);
        }
        return result; 
    }
}
