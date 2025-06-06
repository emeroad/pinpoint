import { ScatterOrHeatmapFullScreenPage, withInitialFetch } from '@pinpoint-fe/ui';
import { getLayoutWithSideNavigation } from '@pinpoint-fe/web/src/components/Layout/LayoutWithSideNavigation';

export default withInitialFetch((props) => {
  return getLayoutWithSideNavigation(<ScatterOrHeatmapFullScreenPage {...props} />);
});
