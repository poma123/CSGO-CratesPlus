package plus.crates.csgo;

import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import plus.crates.Crate;
import plus.crates.Opener.Opener;
import plus.crates.Winning;

import java.io.IOException;
import java.util.*;

public class CSGOOpener extends Opener {
	//private HashMap<UUID, BukkitTask> tasks = new HashMap<>();
	private HashMap<UUID, Inventory> guis = new HashMap<>();
	private int length = 10;

	public CSGOOpener(Plugin plugin, String name) {
		super(plugin, name);
	}

	@Override
	public void doSetup() {
		FileConfiguration config = getOpenerConfig();
		if (!config.isSet("Length")) {
			config.set("Length", 10);
			try {
				config.save(getOpenerConfigFile());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		length = config.getInt("Length");
	}

	@Override
	public void doReopen(Player player, Crate crate, Location blockLocation) {
		player.openInventory(guis.get(player.getUniqueId()));
	}

	@Override
	public void doOpen(final Player player, final Crate crate, Location blockLocation) {
		final Inventory winGUI;
		final Integer[] timer = {0};
		winGUI = Bukkit.createInventory(null, 27, crate.getColor() + crate.getName() + " Win");
		guis.put(player.getUniqueId(), winGUI);
		player.openInventory(winGUI);
		final int maxTimeTicks = length * 10;
		final int slowSpeedTime = maxTimeTicks / 20;
		final int fastSpeedTime = (maxTimeTicks / 10) * 9;
		final ArrayList<Winning> last5Winnings = new ArrayList<>();
		/*tasks.put(player.getUniqueId(), */
		new BukkitRunnable() {
			public void run() {
				if (!player.isOnline()) {
					finish(player);
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "crate key " + player.getName() + " " + crate.getName() + " 1");
					//Bukkit.getScheduler().cancelTask(tasks.get(player.getUniqueId()));
					this.cancel();
					return;
				}
				if ((timer[0] > fastSpeedTime || timer[0] < slowSpeedTime) && (timer[0] & 1) == 0) {
					timer[0]++;
					return;
				}
				Integer i = 0;
				while (i < 27) {

					if (i == 4 || i == 22) {
						ItemStack torch = new ItemStack(Material.REDSTONE_TORCH);
						ItemMeta itemMeta = torch.getItemMeta();
						itemMeta.setDisplayName(ChatColor.GREEN + " ");
						torch.setItemMeta(itemMeta);
						winGUI.setItem(i, torch);
						i++;
						continue;
					}

					Winning winning;
					if (i >= 10 && i <= 16) {

						if (i == 16) {

							winning = getWinning(crate);//crate.getRandomWinning();
							if (last5Winnings.size() == 3)
								last5Winnings.remove(0);
							last5Winnings.add(winning);
							winGUI.setItem(i, winning.getPreviewItemStack());
						} else if (winGUI.getItem(i + 1) != null) {
							winGUI.setItem(i, winGUI.getItem(i + 1));
						}
						if (i == 13) {

							if (timer[0] >= maxTimeTicks) {
								winning = last5Winnings.get(0);
								winning.runWin(player);
								//crate.handleWin(player, winning);
							}

						}

						i++;
						continue;
					}

					List<String> l = Arrays.asList("WHITE", "ORANGE", "MAGENTA", "LIGHT_BLUE", "YELLOW", "LIME", "PINK", "GRAY", "LIGHT_GRAY", "CYAN", "PURPLE", "BLUE", "BROWN", "GREEN", "RED", "BLACK");



					ItemStack itemStack = new ItemStack(Material.getMaterial(l.get(new Random().nextInt(15)) + "_STAINED_GLASS_PANE"), 1);
					ItemMeta itemMeta = itemStack.getItemMeta();
					if (timer[0] >= maxTimeTicks) {
						itemMeta.setDisplayName(ChatColor.RESET + "Winner!");
					} else {
						/*Sound sound;
						try {
							sound = Sound.BLOCK_NOTE_BLOCK_PLING;
						} catch (Exception e) {
							try {
								sound = Sound.BLOCK_NOTE_BLOCK_HARP;
							} catch (Exception ee) {
								return; // This should never happen!
							}
						}
						final Sound finalSound = sound;
						new BukkitRunnable() {
							@Override
							public void run() {
								if (player.getOpenInventory().getTitle() != null && player.getOpenInventory().getTitle().contains(" Win"))
									player.playSound(player.getLocation(), finalSound, (float) 0.2, 2);
							}
						}.runTask(getPlugin());*/
						itemMeta.setDisplayName(ChatColor.RESET + "Rolling...");
					}
					itemStack.setItemMeta(itemMeta);
					winGUI.setItem(i, itemStack);
					i++;
				}
				if (timer[0] >= maxTimeTicks) {
					finish(player);
					//Bukkit.getScheduler().cancelTask(tasks.get(player.getUniqueId()));
					this.cancel();
					return;
				}
				timer[0]++;
			}
		}.runTaskTimerAsynchronously( getPlugin(), 0L, 2L);
	}



}