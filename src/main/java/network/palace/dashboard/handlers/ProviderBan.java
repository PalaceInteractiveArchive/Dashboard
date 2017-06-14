package network.palace.dashboard.handlers;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class ProviderBan {
    @Getter private String provider;
    @Getter private String source;
}