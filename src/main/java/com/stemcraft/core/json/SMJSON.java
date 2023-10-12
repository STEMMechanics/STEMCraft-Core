package com.stemcraft.core.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import org.bukkit.FireworkEffect;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionType;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SMJSON {
    @SuppressWarnings("deprecation")
	public static String toJson(@Nullable ItemStack item) {
		if (item == null)
			return "";

		Map<String, Object> itemMap = new HashMap<>();

        itemMap.put("type", item.getType().name());

		if (item.getDurability() > 0)
			itemMap.put("data", item.getDurability());

		if (item.getAmount() != 1)
			itemMap.put("amount", item.getAmount());

		if (item.hasItemMeta()) {
			final Map<String, Object> itemMetaMap = new HashMap<>();
			final ItemMeta meta = item.getItemMeta();

			if (meta.hasDisplayName())
				itemMetaMap.put("displayname", meta.getDisplayName());

			if (meta.hasLore()) {
				itemMetaMap.put("lore", meta.getLore());
			}

			if (meta.hasEnchants()) {
				List<String> enchants = new ArrayList<>();

				meta.getEnchants().forEach((enchantment, integer) -> {
					enchants.add(enchantment.getName() + ":" + integer);
				});

				itemMetaMap.put("enchants", enchants);
			}

			if (!meta.getItemFlags().isEmpty()) {
				List<String> flags = new ArrayList<>();

				meta.getItemFlags().stream().map(ItemFlag::name).forEach(flag -> flags.add(flag));
				itemMetaMap.put("flags", flags);
			}

			if (meta instanceof SkullMeta) {
				final SkullMeta skullMeta = (SkullMeta) meta;

				if (skullMeta.hasOwner()) {
					final Map<String, Object> extraMeta = new HashMap<>();

					extraMeta.put("owner", skullMeta.getOwner());
					itemMetaMap.put("extra-meta", extraMeta);
				}

			} else if (meta instanceof BannerMeta) {
				final BannerMeta bannerMeta = (BannerMeta) meta;
				final Map<String, Object> extraMeta = new HashMap<>();
				extraMeta.put("base-color", bannerMeta.getBaseColor().name());

				if (bannerMeta.numberOfPatterns() > 0) {
					List<String> patterns = new ArrayList<>();
					bannerMeta.getPatterns()
							.stream()
							.map(pattern -> pattern.getColor().name() + ":" + pattern.getPattern().getIdentifier())
							.forEach(str -> patterns.add(str));
					extraMeta.put("patterns", patterns);
				}

				itemMetaMap.put("extra-meta", extraMeta);

			} else if (meta instanceof EnchantmentStorageMeta) {
				final EnchantmentStorageMeta esmeta = (EnchantmentStorageMeta) meta;

				if (esmeta.hasStoredEnchants()) {
					final Map<String, Object> extraMeta = new HashMap<>();
                    List<String> storedEnchants = new ArrayList<>();

					esmeta.getStoredEnchants().forEach((enchantment, integer) -> {
						storedEnchants.add(enchantment.getName() + ":" + integer);
					});

					extraMeta.put("stored-enchants", storedEnchants);
					itemMetaMap.put("extra-meta", extraMeta);
				}

			} else if (meta instanceof LeatherArmorMeta) {
				final LeatherArmorMeta lameta = (LeatherArmorMeta) meta;
				final Map<String, Object> extraMeta = new HashMap<>();

				extraMeta.put("color", Integer.toHexString(lameta.getColor().asRGB()));
				itemMetaMap.put("extra-meta", extraMeta);

			} else if (meta instanceof BookMeta) {
				final BookMeta bmeta = (BookMeta) meta;

				if (bmeta.hasAuthor() || bmeta.hasPages() || bmeta.hasTitle()) {
					final Map<String, Object> extraMeta = new HashMap<>();

					if (bmeta.hasTitle())
						extraMeta.put("title", bmeta.getTitle());

					if (bmeta.hasAuthor())
						extraMeta.put("author", bmeta.getAuthor());

					if (bmeta.hasPages()) {
                        List<String> pages = new ArrayList<>();

						bmeta.getPages().forEach(str -> pages.add(str));
						extraMeta.put("pages", pages);
					}

					itemMetaMap.put("extra-meta", extraMeta);
				}

			} else if (meta instanceof PotionMeta) {
				final PotionMeta pmeta = (PotionMeta) meta;

				final Map<String, Object> extraMeta = new HashMap<>();

				if (pmeta.hasCustomEffects()) {
                    final List<String> customEffects = new ArrayList<>();

					pmeta.getCustomEffects().forEach(potionEffect -> {
						customEffects.add(potionEffect.getType().getName()
								+ ":" + potionEffect.getAmplifier()
								+ ":" + potionEffect.getDuration() / 20);
					});

					extraMeta.put("custom-effects", customEffects);

				} else
					try {
						final PotionType type = pmeta.getBasePotionData().getType();
						final boolean isExtended = pmeta.getBasePotionData().isExtended();
						final boolean isUpgraded = pmeta.getBasePotionData().isUpgraded();

                        final Map<String, Object> baseEffect = new HashMap<>();

						baseEffect.put("type", type.getEffectType().getName());
						baseEffect.put("isExtended", isExtended);
						baseEffect.put("isUpgraded", isUpgraded);
						extraMeta.put("base-effect", baseEffect);

					} catch (final NoSuchMethodError err) {
						// Unsupported
					}

				itemMetaMap.put("extra-meta", extraMeta);

			} else if (meta instanceof FireworkEffectMeta) {
				final FireworkEffectMeta femeta = (FireworkEffectMeta) meta;

				if (femeta.hasEffect()) {
					final FireworkEffect effect = femeta.getEffect();
					final Map<String, Object> extraMeta = new HashMap<>();

					extraMeta.put("type", effect.getType().name());
					if (effect.hasFlicker())
						extraMeta.put("flicker", true);
					if (effect.hasTrail())
						extraMeta.put("trail", true);

					if (!effect.getColors().isEmpty()) {
                        final List<String> colors = new ArrayList<>();
						effect.getColors().forEach(color -> colors.add(Integer.toHexString(color.asRGB())));
						extraMeta.put("colors", colors);
					}

					if (!effect.getFadeColors().isEmpty()) {
                        final List<String> fadeColors = new ArrayList<>();

						effect.getFadeColors().forEach(color -> fadeColors.add(Integer.toHexString(color.asRGB())));
						extraMeta.put("fade-colors", fadeColors);
					}

					itemMetaMap.put("extra-meta", extraMeta);
				}

			} else if (meta instanceof FireworkMeta) {
				final FireworkMeta fmeta = (FireworkMeta) meta;
				final Map<String, Object> extraMeta = new HashMap<>();

				extraMeta.put("power", fmeta.getPower());

				if (fmeta.hasEffects()) {
                    final List<Object> effects = new ArrayList<>();

					fmeta.getEffects().forEach(effect -> {
                        final Map<String, Object> jsonObject = new HashMap<>();

						jsonObject.put("type", effect.getType().name());

						if (effect.hasFlicker())
							jsonObject.put("flicker", true);

						if (effect.hasTrail())
							jsonObject.put("trail", true);

						if (!effect.getColors().isEmpty()) {
                            final List<String> colors = new ArrayList<>();
							effect.getColors().forEach(color -> colors.add(Integer.toHexString(color.asRGB())));
							jsonObject.put("colors", colors);
						}

						if (!effect.getFadeColors().isEmpty()) {
                            final List<String> fadeColors = new ArrayList<>();

							effect.getFadeColors().forEach(color -> fadeColors.add(Integer.toHexString(color.asRGB())));
							jsonObject.put("fade-colors", fadeColors);
						}

						effects.add(jsonObject);
					});

					extraMeta.put("effects", effects);
				}
				itemMetaMap.put("extra-meta", extraMeta);

			} else if (meta instanceof MapMeta) {
				final MapMeta mmeta = (MapMeta) meta;
				final Map<String, Object> extraMeta = new HashMap<>();

				try {
					if (mmeta.hasLocationName())
						extraMeta.put("location-name", mmeta.getLocationName());

					if (mmeta.hasColor())
						extraMeta.put("color", Integer.toHexString(mmeta.getColor().asRGB()));

				} catch (final NoSuchMethodError err) {
					// Unsupported
				}

				extraMeta.put("scaling", mmeta.isScaling());

				itemMetaMap.put("extra-meta", extraMeta);
			}

			itemMap.put("item-meta", itemMetaMap);
		}

		return toJSON(itemMap);
	}
    public static String toJSON(Object item) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            return mapper.writeValueAsString(item);
        } catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static <T> T fromJSON(String item, Class<T> clazz) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            return mapper.readValue(item, clazz);
        } catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}