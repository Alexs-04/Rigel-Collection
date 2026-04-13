import React from 'react';
import {useAppSettings} from '../context/AppSettingsContext';

class ErrorBoundaryImpl extends React.Component {
    constructor(props) {
        super(props);
        this.state = {error: null, info: null};
    }

    componentDidCatch(error, info) {
        console.error('ErrorBoundary caught', error, info);
        this.setState({error, info});
    }

    render() {
        if (this.state.error) {
            return (
                <div style={{padding: 24}}>
                    <h2 style={{color: '#ff6b6b'}}>Ha ocurrido un error en la app</h2>
                    {this.props.showFrontendErrors ? (
                        <>
                            <pre style={{
                                whiteSpace: 'pre-wrap',
                                background: '#111',
                                color: '#eee',
                                padding: 12
                            }}>{String(this.state.error)}</pre>
                            <details style={{whiteSpace: 'pre-wrap', marginTop: 12}}>
                                {this.state.info && this.state.info.componentStack}
                            </details>
                        </>
                    ) : (
                        <p>Intenta recargar la página o vuelve a iniciar sesión.</p>
                    )}
                </div>
            );
        }
        return this.props.children;
    }
}

export default function ErrorBoundary(props) {
    const {settings} = useAppSettings();
    return <ErrorBoundaryImpl {...props} showFrontendErrors={settings.showFrontendErrors}/>;
}

