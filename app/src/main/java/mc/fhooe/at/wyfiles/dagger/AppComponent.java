package mc.fhooe.at.wyfiles.dagger;

import javax.inject.Singleton;

import dagger.Component;
import mc.fhooe.at.wyfiles.core.ConnectionActivity;
import mc.fhooe.at.wyfiles.core.MainActivity;
import mc.fhooe.at.wyfiles.fragments.BattleshipsFragment;
import mc.fhooe.at.wyfiles.fragments.ConnectionFragment;
import mc.fhooe.at.wyfiles.fragments.FilesFragment;
import mc.fhooe.at.wyfiles.fragments.GamesFragment;

/**
 * @author Martin Macheiner
 *         Date: 18.12.2016.
 */

@Singleton
@Component(modules = {AppModule.class})
public interface AppComponent {

    void inject(ConnectionActivity activity);

    void inject(MainActivity activity);

    void inject(ConnectionFragment fragment);

    void inject(FilesFragment fragment);

    void inject(GamesFragment fragment);

    void inject(BattleshipsFragment fragment);

}
