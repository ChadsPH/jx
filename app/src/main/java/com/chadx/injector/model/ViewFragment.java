package com.chadx.injector.model;

import com.chadx.injector.a;
import androidx.fragment.app.Fragment;
import android.view.View;

public abstract class ViewFragment extends Fragment
	implements OnUpdateLayout
{
	public void updateLayout()
	{
		updateLayout(null);
	}
}
